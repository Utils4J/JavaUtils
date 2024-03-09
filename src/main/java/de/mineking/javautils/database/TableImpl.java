package de.mineking.javautils.database;

import de.mineking.javautils.database.exception.ConflictException;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.Update;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableImpl<T> implements InvocationHandler, Table<T> {
	private final Supplier<Table<T>> table;

	private final String name;
	private final Supplier<T> instance;
	private final DatabaseManager manager;

	private final Map<String, Field> columns = new LinkedHashMap<>();
	private final Map<String, Field> keys = new LinkedHashMap<>();
	private final Map<String, Field> unique = new LinkedHashMap<>();

	TableImpl(DatabaseManager manager, Supplier<Table<T>> table, Class<T> type, Supplier<T> instance, String name) {
		this.manager = manager;
		this.table = table;
		this.instance = instance;
		this.name = name;

		for(var f : type.getDeclaredFields()) {
			if(!f.isAnnotationPresent(Column.class)) continue;

			f.setAccessible(true);

			columns.put(getColumnName(f), f);
			if(f.getAnnotation(Column.class).key()) keys.put(getColumnName(f), f);
			if(f.getAnnotation(Column.class).key() || f.getAnnotation(Column.class).unique()) unique.put(getColumnName(f), f);
		}
	}

	@NotNull
	@Override
	public DatabaseManager getManager() {
		return manager;
	}

	@NotNull
	@Override
	public String getName() {
		return name;
	}

	@NotNull
	@Override
	public Table<T> createTable() {
		var columns = Stream.concat(
				this.keys.entrySet().stream(),
				this.columns.entrySet().stream().filter(e -> !keys.containsKey(e.getKey()))
		).map(e -> '"' + e.getKey() + "\" " +
				manager.getType(e.getValue().getGenericType(), e.getValue()).getName() + " " +
				e.getValue().getAnnotation(Column.class).modifier() +
				(e.getValue().getAnnotation(Column.class).unique() ? " unique" : "")
		).collect(Collectors.joining(", "));

		if(!this.keys.isEmpty()) columns += ", primary key(" + this.keys.keySet().stream().map(field -> '"' + field + '"').collect(Collectors.joining(", ")) + ")";

		final var fColumns = columns; //Because java

		manager.db.useHandle(handle -> handle.createUpdate("create table if not exists <name>(<columns>)")
				.define("name", name)
				.define("columns", fColumns)
				.execute()
		);

		return table.get();
	}

	@NotNull
	@Override
	public Map<String, Field> getColumns() {
		return columns;
	}

	@NotNull
	@Override
	public Map<String, Field> getUnique() {
		return unique;
	}

	@NotNull
	@Override
	public Map<String, Field> getKeys() {
		return keys;
	}

	@NotNull
	@Override
	public Optional<T> selectOne(@NotNull Where where) {
		return manager.db.withHandle(handle -> handle.createQuery("select * from <name> <where>")
				.define("name", name)
				.define("where", where.format())
				.bindMap(where.formatValues(this))
				.map(this::createObject)
				.findFirst()
		);
	}

	@NotNull
	@Override
	public List<T> selectMany(@NotNull Where where, @NotNull Order order) {
		return manager.db.withHandle(handle -> handle.createQuery("select * from <name> <where> <order>")
				.define("name", name)
				.define("where", where.format())
				.bindMap(where.formatValues(this))
				.define("order", order.format())
				.map(this::createObject)
				.list()
		);
	}

	private T createObject(ResultSet set, StatementContext context) {
		var instance = this.instance.get();

		columns.forEach((name, field) -> {
			try {
				field.set(instance, manager.parse(field.getGenericType(), field, manager.extract(field.getGenericType(), field, name, set)));
			} catch(IllegalAccessException | SQLException e) {
				throw new RuntimeException(e);
			}
		});

		return instance;
	}

	static String getColumnName(Field field) {
		var column = field.getAnnotation(Column.class);
		return (column == null || column.name().isEmpty() ? field.getName() : column.name()).toLowerCase();
	}

	@Override
	public int delete(@NotNull Where where) {
		return manager.db.withHandle(handle -> handle.createUpdate("delete from <name> <where>")
				.define("name", name)
				.define("where", where.format())
				.bindMap(where.formatValues(this))
				.execute()
		);
	}

	private boolean execute(@NotNull T object, @NotNull Update query) {
		columns.forEach((name, field) -> {
			try {
				query.bind(name, manager.getArgument(field.getGenericType(), field, field.get(object)));
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});

		return query.execute((statementSupplier, ctx) -> {
			var stmt = statementSupplier.get();
			var rs = stmt.getResultSet();

			if(rs.next()) {
				columns.forEach((name, field) -> {
					try {
						field.set(object, manager.parse(field.getGenericType(), field, manager.extract(field.getGenericType(), field, name, rs)));
					} catch(IllegalAccessException | SQLException e) {
						throw new RuntimeException(e);
					}
				});
				return true;
			} else return false;
		});
	}

	@NotNull
	@Override
	public T insert(@NotNull T object) throws ConflictException {
		var check = Where.detectConflict(this, object, true);
		var sql = "insert into <name>(<columns>) (select <values> where not exists (select from <name> <where>)) returning *";

		var updated = manager.db.withHandle(handle -> execute(object, handle.createUpdate(sql)
				.define("name", name)
				.define("columns", columns.entrySet().stream()
						.filter(e -> {
							try {
								return !(e.getValue().getAnnotation(Column.class).autoincrement() && ((Number) e.getValue().get(object)).longValue() <= 0);
							} catch(IllegalAccessException ex) {
								throw new RuntimeException(ex);
							}
						})
						.map(e -> '"' + e.getKey() + '"')
						.collect(Collectors.joining(", "))
				)
				.define("values", columns.entrySet().stream()
						.filter(e -> {
							try {
								return !(e.getValue().getAnnotation(Column.class).autoincrement() && ((Number) e.getValue().get(object)).longValue() <= 0);
							} catch(IllegalAccessException ex) {
								throw new RuntimeException(ex);
							}
						})
						.map(e -> ":" + e.getKey())
						.collect(Collectors.joining(", "))
				)
				.define("where", check.format())
				.bindMap(check.formatValues(this))
		));

		if(updated) return object;
		else throw new ConflictException();
	}

	@NotNull
	@Override
	public T update(@NotNull T object) throws ConflictException {
		var identifier = Where.identify(this, object);
		var unique = Where.detectConflict(this, object, false);

		var sql = "update <name> set <update> <where>";
		if(this.unique.size() > keys.size()) sql += " and not exists (select from <name> <unique>)";
		final var fSql = sql + " returning *";

		var updated = manager.db.withHandle(handle -> execute(object, handle.createUpdate(fSql)
				.define("name", name)
				.define("update", columns.keySet().stream()
						.filter(k -> !this.keys.containsKey(k))
						.map(k -> '"' + k + "\" = :" + k)
						.collect(Collectors.joining(", "))
				)
				.define("where", identifier.format())
				.define("unique", unique.format())
				.bindMap(identifier.formatValues(this))
				.bindMap(unique.formatValues(this))
		));

		if(updated) return object;
		else throw new ConflictException();
	}

	@Override
	public void updateField(@NotNull Where where, @NotNull String name, @NotNull Object value) {
		var field = columns.get(name);
		if(field == null) throw new IllegalArgumentException("Column '" + name + "' not found");

		manager.db.useHandle(handle -> handle.createUpdate("update <table> set <name> = :value <where>")
				.define("table", this.name)
				.define("name", name)
				.define("where", where.format())
				.bind("value", manager.getArgument(field.getGenericType(), field, value))
				.bindMap(where.formatValues(this))
				.execute()
		);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Table<?> t && t.getName().equals(name);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			return getClass().getMethod(method.getName(), Arrays.stream(method.getParameters()).map(Parameter::getType).toArray(Class[]::new)).invoke(this, args);
		} catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch(InvocationTargetException e) {
			throw e.getCause();
		} catch(NoSuchMethodException e) {
			return InvocationHandler.invokeDefault(proxy, method, args);
		}
	}
}
