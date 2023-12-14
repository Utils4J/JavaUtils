package de.mineking.javautils.database;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DatabaseManager {
	final List<TypeMapper<?>> mappers = new ArrayList<>();
	final Jdbi db;

	public DatabaseManager(@NotNull String host, @NotNull String user, @NotNull String password) {
		db = Jdbi.create(host, user, password);

		mappers.add(TypeMapper.SERIAL);
		mappers.add(TypeMapper.INTEGER);
		mappers.add(TypeMapper.LONG);
		mappers.add(TypeMapper.DOUBLE);
		mappers.add(TypeMapper.BOOLEAN);
		mappers.add(TypeMapper.STRING);
		mappers.add(TypeMapper.TIMESTAMP);
		mappers.add(TypeMapper.OPTIONAL);
		mappers.add(TypeMapper.ENUM);
		mappers.add(TypeMapper.ARRAY);
	}

	@NotNull
	public DatabaseManager addMapper(@NotNull TypeMapper<?> mapper) {
		mappers.add(0, mapper);
		return this;
	}

	@NotNull
	public TypeMapper<?> getMapper(@NotNull Class<?> type, @NotNull Field f) {
		return mappers.stream()
				.filter(m -> m.accepts(this, type, f))
				.findFirst().orElseThrow();
	}

	@NotNull
	public String getType(@NotNull Class<?> type, @NotNull Field field) {
		return getMapper(type, field).getType(this, type, field);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T> T parse(@NotNull Class<T> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set, @NotNull StatementContext context) throws SQLException {
		return (T) getMapper(type, field).parse(this, type, field, name, set, context);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T> Object value(@NotNull Field field, @Nullable T value) {
		return ((TypeMapper<T>) getMapper(field.getType(), field)).value(this, field, value);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <O, T extends ITable<O>> T getTable(@NotNull Class<O> type, @NotNull Supplier<O> instance, @NotNull String name) {
		return (T) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class<?>[] { type },
				new Table<>(this, type, instance, name)
		);
	}

	@NotNull
	public <O, T extends ITable<O>> T getTable(@NotNull Class<O> type, @NotNull Supplier<O> instance) {
		return getTable(type, instance, type.getSimpleName().toLowerCase());
	}
}
