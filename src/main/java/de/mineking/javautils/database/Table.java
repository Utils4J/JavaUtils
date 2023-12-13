package de.mineking.javautils.database;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Table<T> implements InvocationHandler, ITable<T> {
	private final String name;
	private final Class<T> type;
	private final Supplier<T> instance;
	private final Jdbi db;

	Table(Jdbi db, Class<T> type, Supplier<T> instance, String name) {
		this.db = db;
		this.type = type;
		this.instance = instance;
		this.name = name;
	}

	@Override
	public void createTable() {
		db.useHandle(handle -> handle.createUpdate("create table <name>(<columns>)")
				.define("name", name)
				.define("columns", null) //TODO
				.execute()
		);
	}

	@NotNull
	@Override
	public Optional<T> selectOne(@NotNull Where where) {
		return db.withHandle(handle -> handle.createQuery("select * from <name><where>")
				.define("name", name)
				.define("where", where.format())
				.map(this::createObject)
				.findFirst()
		);
	}

	@NotNull
	@Override
	public List<T> selectMany(@NotNull Where where, @NotNull Order order) {
		return db.withHandle(handle -> handle.createQuery("select * from <name> <where> <order>")
				.define("name", name)
				.define("where", where.format())
				.define("order", order.format())
				.map(this::createObject)
				.list()
		);
	}

	private T createObject(ResultSet set, StatementContext context) {
		var instance = this.instance.get();

		//TODO

		return instance;
	}

	@Override
	public void delete(@NotNull Where where) {
		//TODO
	}

	@Override
	public void delete(@NotNull T object) {
		//TODO
	}

	@NotNull
	@Override
	public T insert(@NotNull T object) {
		return null; //TODO
	}

	@NotNull
	@Override
	public <C extends Collection<T>> C insertMany(@NotNull C objects) {
		return null; //TODO
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
