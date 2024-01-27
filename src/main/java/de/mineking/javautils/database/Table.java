package de.mineking.javautils.database;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Table<T> {
	@NotNull
	DatabaseManager getManager();

	@NotNull
	String getName();


	@NotNull
	Table<T> createTable();

	@NotNull
	Map<String, Field> getColumns();

	@NotNull
	Map<String, Field> getKeys();


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
	default List<T> insertMany(@NotNull Collection<T> objects) {
		return objects.stream()
				.map(this::insert)
				.toList();
	}

	int delete(@NotNull Where where);

	default int delete(@NotNull T object) {
		return delete(Where.of(this, object));
	}

	default int deleteAll() {
		return delete(Where.empty());
	}
}
