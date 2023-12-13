package de.mineking.javautils.database;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ITable<T> {
	void createTable();

	@NotNull
	Optional<T> selectOne(@NotNull Where where);

	@NotNull
	List<T> selectMany(@NotNull Where where);

	@NotNull
	default List<T> selectAll() {
		return selectMany(Where.empty());
	}

	@NotNull
	T insert(@NotNull T object);

	@NotNull
	<C extends Collection<T>> C insertMany(@NotNull C objects);

	void delete(@NotNull Where where);

	void delete(@NotNull T object);
}
