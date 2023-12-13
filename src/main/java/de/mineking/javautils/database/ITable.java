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
	List<T> selectMany(@NotNull Where where, @NotNull Order oder);

	default List<T> selectMany(@NotNull Where where) {
		return selectMany(where, Order.empty());
	}

	@NotNull
	default List<T> selectAll(@NotNull Order order) {
		return selectMany(Where.empty(), order);
	}

	@NotNull
	default List<T> selectAll() {
		return selectAll(Order.empty());
	}

	@NotNull
	T insert(@NotNull T object);

	@NotNull
	<C extends Collection<T>> C insertMany(@NotNull C objects);

	void delete(@NotNull Where where);

	void delete(@NotNull T object);
}
