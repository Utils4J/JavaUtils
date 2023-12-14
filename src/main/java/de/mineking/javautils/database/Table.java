package de.mineking.javautils.database;

import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Table<T> implements InvocationHandler, ITable<T> {
	private final String name;
	private final Supplier<T> instance;
	private final DatabaseManager manager;

	private final Map<String, Field> columns = new HashMap<>();

	Table(DatabaseManager manager, Class<T> type, Supplier<T> instance, String name) {
		this.manager = manager;
		this.instance = instance;
		this.name = name;

		for(var f : type.getDeclaredFields()) {
			if(!f.isAnnotationPresent(Column.class)) continue;
			columns.put(getColumnName(f), f);
		}
	}

	@Override
	public void createTable() {
		var columns = this.columns.entrySet().stream()
				.map(e -> "'" + e.getKey() + "' " + manager.getType(e.getValue().getType(), e.getValue()))
				.collect(Collectors.joining(", "));

		var keys = this.columns.entrySet().stream()
				.filter(e -> e.getValue().getAnnotation(Column.class).key())
				.map(e -> "'" + e.getKey() + "'")
				.collect(Collectors.joining(", "));

		if(!keys.isEmpty()) columns += ", primary key(" + keys + ")";

		final var fColumns = columns; //Because java

		manager.db.useHandle(handle -> handle.createUpdate("create table <name>(<columns>)")
				.define("name", name)
				.define("columns", fColumns)
				.execute()
		);
	}

	@NotNull
	@Override
	public Optional<T> selectOne(@NotNull Where where) {
		return manager.db.withHandle(handle -> handle.createQuery("select * from <name><where>")
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
				field.set(instance, manager.parse(field.getType(), field, name, set, context));
			} catch(IllegalAccessException | SQLException e) {
				throw new RuntimeException(e);
			}
		});

		return instance;
	}

	private String getColumnName(Field field) {
		var column = field.getAnnotation(Column.class);
		return column == null ? field.getName() : column.name();
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
		var keys = manager.db.withHandle(handle -> handle.createUpdate("insert into <name>(<columns>) values(<values>)")
				.define("name", name)
				.define("columns", null)
				.define("values", null) //TODO
				.executeAndReturnGeneratedKeys()
				.mapToMap().one()
		);

		keys.forEach((name, value) -> {
			try {
				columns.get(name).set(object, value);
			} catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});

		return object;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			return getClass().getMethod(method.getName(), Arrays.stream(method.getParameters()).map(Parameter::getType).toArray(Class[]::new)).invoke(proxy, args);
		} catch(IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch(NoSuchMethodException e) {
			return method.invoke(proxy, args);
		}
	}
}
