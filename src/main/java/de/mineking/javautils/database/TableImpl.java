package de.mineking.javautils.database;

import de.mineking.javautils.database.exception.ConflictException;
import org.jdbi.v3.core.result.ResultProducers;
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
		).map(e -> '"' + e.getKey() + "\" " + manager.getType(e.getValue().getGenericType(), e.getValue()).getName()).collect(Collectors.joining(", "));

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

	private boolean execute(@NotNull T object, Update query) {
		columns.forEach((name, field) -> {
			try {
				query.bind(name, manager.getArgument(field.getGenericType(), field, field.get(object)));
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});

		return query.execute(ResultProducers.returningResults())
				.map((rs, ctx) -> {
					columns.forEach((name, field) -> {
						try {
							field.set(object, manager.parse(field.getGenericType(), field, manager.extract(field.getGenericType(), field, name, rs)));
						} catch(IllegalAccessException | SQLException e) {
							throw new RuntimeException(e);
						}
					});
					return true;
				}).findFirst().orElse(false);
	}

	@NotNull
	@Override
	public T insert(@NotNull T object) throws ConflictException {
		var sql = "insert into <name>(<columns>) values(<values>) on conflict do nothing returning *";

		var updated = manager.db.withHandle(handle -> {
			var query = handle.createUpdate(sql)
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
					);

			return execute(object, query);
		});

		if(updated) return object;
		else throw new ConflictException();
	}

	@Override
	public boolean update(@NotNull T object) {
		var sql = "update <name> set <update> <where> returning *";
		var identifier = Where.of(this, object);

		return manager.db.withHandle(handle -> {
			var query = handle.createUpdate(sql)
					.define("name", name)
					.define("update", columns.keySet().stream()
							.filter(k -> !this.keys.containsKey(k))
							.map(k -> '"' + k + "\" = :" + k)
							.collect(Collectors.joining(", "))
					)
					.define("where", identifier.format())
					.bindMap(identifier.formatValues(this));

			return execute(object, query);
		});
	}

	@NotNull
	@Override
	public T insertOrUpdate(@NotNull T object) {
		var sql = "insert into <name>(<columns>) values(<values>) ";

		if(!keys.isEmpty()) sql += " on conflict(<keys>) do update set <update>";

		var fSql = sql; //Because java

		manager.db.useHandle(handle -> {
			var query = handle.createUpdate(fSql)
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
					.define("keys", this.keys.keySet().stream()
							.map(k -> '"' + k + '"')
							.collect(Collectors.joining(", "))
					)
					.define("update", columns.keySet().stream()
							.filter(k -> !this.keys.containsKey(k))
							.map(k -> '"' + k + "\" = :" + k)
							.collect(Collectors.joining(", "))
					);

			execute(object, query);
		});

		return object;
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
