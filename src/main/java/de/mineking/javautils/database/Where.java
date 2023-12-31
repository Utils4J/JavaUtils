package de.mineking.javautils.database;

import de.mineking.javautils.ID;
import de.mineking.javautils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface Where {
	@NotNull
	static <T> Where of(@NotNull Table<T> table, @NotNull T object) {
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
	static Where not(@NotNull Where where) {
		return where.not();
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
	default Map<String, Object> formatValues(@NotNull Table<?> table) {
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
			String id = ID.generate().asString();
			return new WhereImpl("\"" + name + "\" " + operator + " :" + id, Map.of(id, new Pair<>(name, value)));
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
	}
}
