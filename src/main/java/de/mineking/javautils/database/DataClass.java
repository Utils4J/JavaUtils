package de.mineking.javautils.database;

import de.mineking.javautils.database.exception.ConflictException;
import org.jetbrains.annotations.NotNull;

public interface DataClass<T extends DataClass<T>> {
	@NotNull
	Table<T> getTable();

	@NotNull
	@SuppressWarnings("unchecked")
	default T insert() throws ConflictException {
		return getTable().insert((T) this);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	default T update() throws ConflictException {
		return getTable().update((T) this);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	default T upsert() throws ConflictException {
		return getTable().upsert((T) this);
	}

	@SuppressWarnings("unchecked")
	default boolean delete() {
		return getTable().delete((T) this) > 0;
	}
}
