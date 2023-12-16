package de.mineking.javautils.database;

import org.jdbi.v3.core.Jdbi;
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
	final List<TypeMapper<?, ?>> mappers = new ArrayList<>();
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
		mappers.add(TypeMapper.ID);
		mappers.add(TypeMapper.OPTIONAL);
		mappers.add(TypeMapper.ENUM);
		mappers.add(TypeMapper.ARRAY);
	}

	@NotNull
	public DatabaseManager addMapper(@NotNull TypeMapper<?, ?> mapper) {
		mappers.add(0, mapper);
		return this;
	}

	@SuppressWarnings("unchecked")
	@NotNull
	public <T, R> TypeMapper<T, R> getMapper(@NotNull Class<?> type, @NotNull Field f) {
		return (TypeMapper<T, R>) mappers.stream()
				.filter(m -> m.accepts(this, type, f))
				.findFirst().orElseThrow(() -> new IllegalStateException("No mapper found for " + type));
	}

	@NotNull
	public String getType(@NotNull Class<?> type, @NotNull Field field) {
		return getMapper(type, field).getType(this, type, field);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T, R> R parse(@NotNull Class<?> type, @NotNull Field field, T value) {
		return (R) getMapper(type, field).parse(this, type, field, value);
	}

	@Nullable
	public Object extract(@NotNull Class<?> type, @NotNull Field field, @NotNull String name, @NotNull ResultSet set) throws SQLException {
		return getMapper(type, field).extract(set, name);
	}

	@Nullable
	public <T> Object value(@NotNull Class<?> type, @NotNull Field field, @Nullable T value) {
		return getMapper(type, field).value(this, type, field, value);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <O, T extends Table<O>> T getTable(@NotNull Class<T> table, @NotNull Class<O> type, @NotNull Supplier<O> instance, @NotNull String name) {
		return (T) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class<?>[]{table},
				new TableImpl<>(this, type, instance, name)
		);
	}

	@NotNull
	public <O, T extends Table<O>> T getTable(@NotNull Class<T> table, @NotNull Class<O> type, @NotNull Supplier<O> instance) {
		return getTable(table, type, instance, type.getSimpleName().toLowerCase());
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <O> Table<O> getTable(@NotNull Class<O> type, @NotNull Supplier<O> instance, @NotNull String name) {
		return getTable(Table.class, type, instance, name);
	}

	@NotNull
	public <O> Table<O> getTable(@NotNull Class<O> type, @NotNull Supplier<O> instance) {
		return getTable(type, instance, type.getSimpleName().toLowerCase());
	}
}
