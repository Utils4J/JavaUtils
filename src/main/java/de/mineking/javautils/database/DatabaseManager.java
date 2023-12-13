package de.mineking.javautils.database;

import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;

public class DatabaseManager {
	private final Jdbi db;

	public DatabaseManager(@NotNull String host, @NotNull String user, @NotNull String password) {
		db = Jdbi.create(host, user, password);
	}

	@SuppressWarnings("unchecked")
	public <O, T extends ITable<O>> T getTable(@NotNull Class<T> type, @NotNull String name) {
		return (T) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class<?>[] { type },
				new Table<T>(db, type, name)
		);
	}

	public <O, T extends ITable<O>> T getTable(@NotNull Class<T> type) {
		return getTable(type, type.getSimpleName().toLowerCase());
	}
}
