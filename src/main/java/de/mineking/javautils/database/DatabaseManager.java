package de.mineking.javautils.database;

import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public class DatabaseManager {
	private final Jdbi db;

	public DatabaseManager(@NotNull String host, @NotNull String user, @NotNull String password) {
		db = Jdbi.create(host, user, password);
	}

	@SuppressWarnings("unchecked")
	public <O, T extends ITable<O>> T getTable(@NotNull Class<O> type, @NotNull Supplier<O> instance, @NotNull String name) {
		return (T) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class<?>[] { type },
				new Table<>(db, type, instance, name)
		);
	}

	public <O, T extends ITable<O>> T getTable(@NotNull Class<O> type, @NotNull Supplier<O> instance) {
		return getTable(type, instance, type.getSimpleName().toLowerCase());
	}
}
