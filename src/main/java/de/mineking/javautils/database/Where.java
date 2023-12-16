package de.mineking.javautils.database;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Where {
	@NotNull
	static <T> Where of(@NotNull Table<T> table, @NotNull T object) {
		if(table.getKeys().isEmpty()) throw new IllegalArgumentException("Cannot delete element without key");
		return allOf(table.getKeys().entrySet().stream()
				.map(e -> {
					try {
						return equals(e.getKey(), table.getManager().value(e.getValue().getType(), e.getValue(), e.getValue().get(object)));
					} catch(IllegalAccessException ex) {
						throw new RuntimeException(ex);
					}
				})
				.toList()
		);
	}

	@NotNull
	static Where empty() {
		return new Where() {
			@NotNull
			@Override
			public String get() {
				return "";
			}

			@NotNull
			@Override
			public String format() {
				return "";
			}

			@NotNull
			@Override
			public Where and(@NotNull Where other) {
				return other;
			}

			@NotNull
			@Override
			public Where or(@NotNull Where other) {
				return other;
			}

			@NotNull
			@Override
			public Where not() {
				return this;
			}
		};
	}

	@NotNull
	static Where of(@NotNull String condition) {
		return () -> condition;
	}

	@NotNull
	static Where allOf(@NotNull Where where, @NotNull Where... others) {
		for(var w : others) where = where.and(w);
		return where;
	}

	@NotNull
	static Where allOf(@NotNull Collection<Where> wheres) {
		return allOf(empty(), wheres.toArray(Where[]::new));
	}

	@NotNull
	static Where anyOf(@NotNull Where where, @NotNull Where... others) {
		for(var w : others) where = where.or(w);
		return where;
	}

	@NotNull
	static Where anyOf(@NotNull Collection<Where> wheres) {
		return anyOf(empty(), wheres.toArray(Where[]::new));
	}

	@NotNull
	static Where noneOf(@NotNull Where where, @NotNull Where... others) {
		for(var w : others) where = where.and(w.not());
		return where;
	}

	@NotNull
	static Where noneOf(@NotNull Collection<Where> wheres) {
		return noneOf(empty(), wheres.toArray(Where[]::new));
	}

	@NotNull
	static Where equals(@NotNull String name, @NotNull Object value) {
		return () -> '"' + name + "\" = '" + value + "'";
	}

	@NotNull
	static Where notEqual(@NotNull String name, @NotNull Object value) {
		return () -> '"' + name + "\" != '" + value + "'";
	}

	@NotNull
	static Where like(@NotNull String name, @NotNull Object value) {
		return () -> '"' + name + "\" like '" + value + "'";
	}

	@NotNull
	static Where likeIgnoreCase(@NotNull String name, @NotNull Object value) {
		return () -> '"' + name + "\" ilike '" + value + "'";
	}

	@NotNull
	static Where greater(@NotNull String name, @NotNull Object value) {
		return () -> '"' + name + "\" > '" + value + "'";
	}

	@NotNull
	static Where lower(@NotNull String name, @NotNull Object value) {
		return () -> '"' + name + "\" < '" + value + "'";
	}

	@NotNull
	static Where greaterOrEqual(@NotNull String name, @NotNull Object value) {
		return () -> '"' + name + "\" >= '" + value + "'";
	}

	@NotNull
	static Where lowerOrEqual(@NotNull String name, @NotNull Object value) {
		return () -> '"' + name + "\" <= '" + value + "'";
	}

	@NotNull
	static Where not(@NotNull Where where) {
		return where.not();
	}

	@NotNull
	default Where and(@NotNull Where other) {
		if(other.get().isEmpty()) return this;
		return () -> "(" + get() + ") and (" + other.get() + ")";
	}

	@NotNull
	default Where or(@NotNull Where other) {
		if(other.get().isEmpty()) return this;
		return () -> "(" + get() + ") or (" + other.get() + ")";
	}

	@NotNull
	default Where not() {
		return () -> "not " + get();
	}

	@NotNull
	String get();

	@NotNull
	default String format() {
		return "where " + get();
	}
}
