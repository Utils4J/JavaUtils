package de.mineking.javautils.database;

import de.mineking.javautils.ID;
import de.mineking.javautils.Pair;
import org.jdbi.v3.core.argument.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public interface Where {
	@NotNull
	static <T> Where identify(@NotNull Table<T> table, @NotNull T object) {
		if(table.getKeys().isEmpty()) throw new IllegalArgumentException("Cannot identify object without keys");
		return allOf(table.getKeys().entrySet().stream()
				.map(e -> {
					try {
						return equals(e.getKey(), e.getValue().get(object));
					} catch(IllegalAccessException ex) {
						throw new RuntimeException(ex);
					}
				})
				.toList()
		);
	}

	@NotNull
	static <T> Where detectConflict(@NotNull Table<T> table, @NotNull T object, boolean isInsert) {
		if(table.getUnique().isEmpty()) return empty();
		var temp = Where.anyOf(table.getUnique().entrySet().stream()
				.filter(e -> !e.getValue().getAnnotation(Column.class).key())
				.map(e -> {
					try {
						return equals(e.getKey(), e.getValue().get(object));
					} catch(IllegalAccessException ex) {
						throw new RuntimeException(ex);
					}
				})
				.toList()
		);

		return isInsert
				? temp.or(identify(table, object))
				: temp.and(not(identify(table, object)));
	}

	@NotNull
	static Where empty() {
		return new Where() {
			@NotNull
			@Override
			public Map<String, Pair<String, Object>> values() {
				return Collections.emptyMap();
			}

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
		return anyOf(where, others).not();
	}

	@NotNull
	static Where noneOf(@NotNull Collection<Where> wheres) {
		return noneOf(empty(), wheres.toArray(Where[]::new));
	}

	@NotNull
	static Where equals(@NotNull String name, @Nullable Object value) {
		return WhereImpl.create(name, value, "=");
	}

	@NotNull
	static Where notEqual(@NotNull String name, @Nullable Object value) {
		return WhereImpl.create(name, value, "!=");
	}

	@NotNull
	static Where like(@NotNull String name, @Nullable Object value) {
		return WhereImpl.create(name, value, "like");
	}

	@NotNull
	static Where likeIgnoreCase(@NotNull String name, @Nullable Object value) {
		return WhereImpl.create(name, value, "ilike");
	}

	@NotNull
	static Where greater(@NotNull String name, @Nullable Object value) {
		return WhereImpl.create(name, value, ">");
	}

	@NotNull
	static Where lower(@NotNull String name, @Nullable Object value) {
		return WhereImpl.create(name, value, "<");
	}

	@NotNull
	static Where greaterOrEqual(@NotNull String name, @Nullable Object value) {
		return WhereImpl.create(name, value, ">=");
	}

	@NotNull
	static Where lowerOrEqual(@NotNull String name, @Nullable Object value) {
		return WhereImpl.create(name, value, "<=");
	}

	@NotNull
	static Where in(@NotNull String name, @NotNull Iterable<?> values) {
		var temp = new ArrayList<>();
		values.forEach(temp::add);

		if(temp.isEmpty()) return FALSE();
		return WhereImpl.create(name, temp, "in", Collectors.joining(", ", "(", ")"));
	}

	@NotNull
	static Where between(@NotNull String name, @NotNull Object lower, @NotNull Object upper) {
		return WhereImpl.create(name, List.of(lower, upper), "between", Collectors.joining(" and "));
	}

	@NotNull
	static Where isNull(@NotNull String name) {
		return Where.unsafe(name + " is null");
	}

	@NotNull
	static Where isNotNull(@NotNull String name) {
		return Where.unsafe(name + " is not null");
	}

	@NotNull
	static Where not(@NotNull Where where) {
		return where.not();
	}

	@NotNull
	static Where TRUE() {
		return Where.unsafe("TRUE");
	}

	@NotNull
	static Where FALSE() {
		return Where.unsafe("FALSE");
	}

	@NotNull
	default Where and(@NotNull Where other) {
		return WhereImpl.combined(this, other, "and");
	}

	@NotNull
	default Where or(@NotNull Where other) {
		return WhereImpl.combined(this, other, "or");
	}

	@NotNull
	default Where not() {
		return new WhereImpl("not " + get(), values());
	}

	@NotNull
	Map<String, Pair<String, Object>> values();

	@SuppressWarnings({"rawtypes", "unchecked"})
	default Map<String, Argument> formatValues(@NotNull Table<?> table) {
		return values().entrySet().stream()
				.map(e -> {
					Field f = table.getColumns().get(e.getValue().key());
					if(f == null) throw new IllegalStateException("Table has no column with name '" + e.getValue().key() + "'");

					TypeMapper mapper = table.getManager().getMapper(f.getType(), f);

					Object value;

					try {
						value = mapper.format(table.getManager(), f.getType(), f, e.getValue().value());
					} catch(IllegalArgumentException | ClassCastException ex) {
						value = e.getValue().value();
					}

					return Map.entry(e.getKey(), mapper.createArgument(table.getManager(), f.getType(), f, value));
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@NotNull
	String get();

	@NotNull
	default String format() {
		return "where " + get();
	}

	@NotNull
	static Where unsafe(@NotNull String str) {
		return new Where() {
			@NotNull
			@Override
			public Map<String, Pair<String, Object>> values() {
				return Collections.emptyMap();
			}

			@NotNull
			@Override
			public String get() {
				return str;
			}

			@Override
			public String toString() {
				return str;
			}
		};
	}

	class WhereImpl implements Where {
		private final String str;
		private final Map<String, Pair<String, Object>> values;

		public WhereImpl(String str, Map<String, Pair<String, Object>> values) {
			this.str = str;
			this.values = values;
		}

		public static Where create(String name, Object value, String operator) {
			var id = ID.generate().asString();
			return new WhereImpl("\"" + name + "\" " + operator + " :" + id, Map.of(id, new Pair<>(name, value)));
		}

		public static Where create(String name, List<Object> values, String operator, Collector<CharSequence, ?, String> collector) {
			var ids = values.stream()
					.map(v -> new Pair<>(ID.generate().asString(), v))
					.toList();
			return new WhereImpl("\"" + name + "\" " + operator + " " + ids.stream().map(p -> ":" + p.key()).collect(collector),
					ids.stream().collect(Collectors.toMap(Pair::key, p -> new Pair<>(name, p.value())))
			);
		}

		public static Where combined(Where a, Where b, String operator) {
			if(a.get().isEmpty()) return b;
			if(b.get().isEmpty()) return a;

			var combined = new HashMap<>(a.values());
			combined.putAll(b.values());

			return new WhereImpl("(" + a.get() + ") " + operator + " (" + b.get() + ")", combined);
		}

		@NotNull
		@Override
		public Map<String, Pair<String, Object>> values() {
			return values;
		}

		@NotNull
		@Override
		public String get() {
			return str;
		}

		@Override
		public String toString() {
			return get();
		}
	}
}
