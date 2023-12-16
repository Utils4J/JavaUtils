package de.mineking.javautils.database;

import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TableImpl<T> implements InvocationHandler, Table<T> {
	private final Supplier<Table<T>> table;

	private final String name;
	private final Supplier<T> instance;
	private final DatabaseManager manager;

	private final Map<String, Field> columns = new HashMap<>();
	private final Map<String, Field> keys = new HashMap<>();

	TableImpl(DatabaseManager manager, Supplier<Table<T>> table, Class<T> type, Supplier<T> instance, String name) {
		this.manager = manager;
		this.table = table;
		this.instance = instance;
		this.name = name;

		for(var f : type.getDeclaredFields()) {
			if(!f.isAnnotationPresent(Column.class)) continue;

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
		var columns = this.columns.entrySet().stream()
				.map(e -> '"' + e.getKey() + "\" " + manager.getType(e.getValue().getType(), e.getValue()))
				.collect(Collectors.joining(", "));

		if(!this.keys.isEmpty()) columns += ", primary key(" +
				this.keys.keySet().stream().map(field -> '"' + field + '"').collect(Collectors.joining(", ")) +
				")";

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
				.define("order", order.format())
				.map(this::createObject)
				.list()
		);
	}

	private T createObject(ResultSet set, StatementContext context) {
		var instance = this.instance.get();

		columns.forEach((name, field) -> {
			try {
				field.set(instance, manager.parse(field.getType(), field, manager.extract(field.getType(), field, name, set)));
			} catch(IllegalAccessException | SQLException e) {
				throw new RuntimeException(e);
			}
		});

		return instance;
	}

	private String getColumnName(Field field) {
		var column = field.getAnnotation(Column.class);
		return (column == null || column.name().isEmpty() ? field.getName() : column.name()).toLowerCase();
	}

	@Override
	public void delete(@NotNull Where where) {
		manager.db.useHandle(handle -> handle.createUpdate("delete from <name> <where>")
				.define("name", name)
				.define("where", where.format())
				.execute()
		);
	}

	@NotNull
	@Override
	public T insert(@NotNull T object) {
		var sql = "insert into <name>(<columns>) values(<values>) ";

		if(!keys.isEmpty()) sql += " on conflict(<keys>) do update set <update>";

		var fSql = sql; //Because java

		manager.db.useHandle(handle -> handle.createUpdate(fSql)
				.define("name", name)
				.define("columns", columns.entrySet().stream()
						.filter(e -> !e.getValue().getAnnotation(Column.class).autoincrement())
						.map(e -> '"' + e.getKey() + '"')
						.collect(Collectors.joining(", "))
				)
				.define("values", columns.entrySet().stream()
						.filter(e -> !e.getValue().getAnnotation(Column.class).autoincrement())
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
				)
				.bindMap(columns.entrySet().stream().collect(HashMap::new, (m, v) -> {
					try {
						m.put(v.getKey(), manager.value(v.getValue().getType(), v.getValue(), v.getValue().get(object)));
					} catch(IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}, HashMap::putAll))
				.executeAndReturnGeneratedKeys()
				.map((rs, ctx) -> {
					columns.forEach((name, field) -> {
						try {
							field.set(object, manager.parse(field.getType(), field, manager.extract(field.getType(), field, name, rs)));
						} catch(IllegalAccessException | SQLException e) {
							throw new RuntimeException(e);
						}
					});

					return null;
				}).one()
		);

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