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

	@SuppressWarnings("unchecked")
	default boolean update() {
		return getTable().update((T) this);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	default T upsert() {
		return getTable().upsert((T) this);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	default DataClass<T> delete() {
		getTable().delete((T) this);
		return this;
	}
}
