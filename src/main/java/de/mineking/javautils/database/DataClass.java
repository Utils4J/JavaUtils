package de.mineking.javautils.database;

import org.jetbrains.annotations.NotNull;

public interface DataClass<T extends DataClass<T>> {
	@NotNull
	Table<T> getTable();

	@NotNull
	@SuppressWarnings("unchecked")
	default DataClass<T> update() {
		getTable().insert((T) this);
		return this;
	}

	@NotNull
	@SuppressWarnings("unchecked")
	default DataClass<T> delete() {
		getTable().delete((T) this);
		return this;
	}
}
