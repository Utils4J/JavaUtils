package de.mineking.javautils.database;

import de.mineking.javautils.ID;
import org.jdbi.v3.core.argument.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public interface TypeMapper<T, R> {
	boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f);

	@NotNull
	String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f);

	@NotNull
	default Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable R value) {
		return (pos, stmt, ctx) -> stmt.setObject(pos, value);
	}

	@NotNull
	default String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable R value) {
		return Objects.toString(value);
	}

	@Nullable
	T extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException;

	@SuppressWarnings("unchecked")
	@Nullable
	default R parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable T value) {
		return (R) value;
	}

	TypeMapper<Integer, Integer> SERIAL = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return f.getAnnotation(Column.class).autoincrement() && (int.class.isAssignableFrom(type) || long.class.isAssignableFrom(type));
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return int.class.isAssignableFrom(type) ? "serial" : "bigserial";
		}

		@Nullable
		@Override
		public Integer extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Integer) set.getObject(name);
		}
	};

	TypeMapper<Integer, Integer> INTEGER = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return int.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "int";
		}

		@Nullable
		@Override
		public Integer extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Integer) set.getObject(name);
		}
	};

	TypeMapper<Long, Long> LONG = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return long.class.isAssignableFrom(type) || Long.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "bigint";
		}

		@Nullable
		@Override
		public Long extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Long) set.getObject(name);
		}
	};

	TypeMapper<Double, Double> DOUBLE = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return double.class.isAssignableFrom(type) || Double.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "decimal";
		}

		@Nullable
		@Override
		public Double extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Double) set.getObject(name);
		}
	};

	TypeMapper<Boolean, Boolean> BOOLEAN = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "boolean";
		}

		@Nullable
		@Override
		public Boolean extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return (Boolean) set.getObject(name);
		}
	};

	TypeMapper<String, String> STRING = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return String.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "text";
		}

		@Nullable
		@Override
		public String extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return set.getString(name);
		}
	};

	TypeMapper<Instant, Instant> TIMESTAMP = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return Instant.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "timestamp";
		}

		@Nullable
		@Override
		public Instant extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			var timestamp = set.getTimestamp(name);
			return timestamp == null ? null : timestamp.toInstant();
		}
	};

	TypeMapper<String, ID> MKID = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return ID.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "text";
		}

		@NotNull
		@Override
		public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable ID value) {
			return (pos, stmt, ctx) -> stmt.setString(pos, value == null ? ID.generate().asString() : value.asString());
		}

		@NotNull
		@Override
		public String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable ID value) {
			return value == null ? ID.generate().asString() : value.asString();
		}

		@Nullable
		@Override
		public String extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return set.getString(name);
		}

		@NotNull
		@Override
		public de.mineking.javautils.ID parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String value) {
			return ID.decode(value);
		}
	};

	TypeMapper<Object, Optional<?>> OPTIONAL = new TypeMapper<>() {
		private final Argument empty = (pos, stmt, ctx) -> stmt.setObject(pos, null);

		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field) {
			return type.equals(Optional.class);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			var p = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
			return manager.getType((Class<?>) p, f);
		}

		@NotNull
		@Override
		public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Optional<?> value) {
			return Optional.ofNullable(value).flatMap(x -> x).map(x -> (Argument) (position, statement, ctx) -> statement.setObject(position, x)).orElse(empty);
		}

		@NotNull
		@Override
		public String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Optional<?> value) {
			var p = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
			return manager.getMapper((Class<?>) p, f).string(manager, (Class<?>) p, f, value.orElse(null));
		}

		@Nullable
		@Override
		public Object extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return set.getObject(name);
		}

		@NotNull
		@Override
		public Optional<?> parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable Object value) {
			var p = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
			return Optional.ofNullable(manager.parse((Class<?>) p, field, value));
		}
	};

	TypeMapper<String, Enum<?>> ENUM = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return type.isEnum();
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "text";
		}

		@NotNull
		@Override
		public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Enum<?> value) {
			return (pos, stmt, ctx) -> stmt.setObject(pos, value == null ? null : value.name());
		}

		@NotNull
		@Override
		public String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Enum<?> value) {
			return value == null ? "null" : value.name();
		}

		@Nullable
		@Override
		public String extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			return set.getString(name);
		}

		@Nullable
		@Override
		public Enum<?> parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable String value) {
			if(value == null) return null;
			return getEnumConstant(type, value).orElse(null);
		}

		private Optional<Enum<?>> getEnumConstant(Class<?> type, String name) {
			return Arrays.stream((Enum<?>[]) type.getEnumConstants())
					.filter(c -> c.name().equals(name))
					.findFirst();
		}
	};

	TypeMapper<Object[], ?> ARRAY = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return type.isArray() || Collection.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			var component = getComponentType(type, f.getGenericType());
			return manager.getType(component, f) + "[]";
		}

		@NotNull
		@Override
		public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Object value) {
			return (pos, stmt, ctx) -> {
				if(value == null) {
					stmt.setArray(pos, null);
					return;
				}

				var t = "text";

				var c = getComponentType(type, f.getGenericType());
				while(c.isArray() || Collection.class.isAssignableFrom(c)) c = getComponentType(c, f.getGenericType());
				t = manager.getType(c, f);

				stmt.setArray(pos, stmt.getConnection().createArrayOf(t, toSql(manager, value, f)));
			};
		}

		private Object[] toSql(DatabaseManager manager, Object o, Field field) {
			var component = getComponentType(o.getClass(), field.getGenericType());

			if(component.isArray() || Collection.class.isAssignableFrom(component)) {
				var maxLength = stream(o).filter(Objects::nonNull).mapToInt(e -> e.getClass().isArray() ? ((Object[]) e).length : ((Collection<?>) e).size()).max().orElse(0);
				if(maxLength == 0) return null;

				return stream(o)
						.map(e -> {
							if(e == null) return new Object[0];
							return e.getClass().isArray() ? (Object[]) e : ((Collection<?>) e).toArray(i -> (Object[]) Array.newInstance(getComponentType(component, field.getGenericType()), i));
						})
						.map(e -> Arrays.copyOf(e, maxLength))
						.map(e -> toSql(manager, e, field))
						.toArray();
			}

			return stream(o).map(e -> manager.getMapper(component, field).string(manager, component, field, e)).toArray();
		}

		private Stream<?> stream(Object o) {
			return o.getClass().isArray() ? Arrays.stream((Object[]) o) : ((Collection<?>) o).stream();
		}

		@Nullable
		@Override
		public Object[] extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
			var temp = set.getArray(name);
			if(temp == null) return null;
			else return (Object[]) temp.getArray();
		}

		@Nullable
		@Override
		public Object parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable Object[] value) {
			var component = getComponentType(type, field.getGenericType());
			if(value == null) return createCollection(type, component, Collections.emptyList());

			var array = Arrays.stream(value)
					.filter(o -> component.isArray() || Collection.class.isAssignableFrom(component) || o != null)
					.map(e -> manager.parse(component, field, e))
					.toList();

			return type.isArray() ? array.toArray(i -> (Object[]) Array.newInstance(component, i)) : createCollection(type, component, array);
		}

		private Class<?> getComponentType(Class<?> type, Type generic) {
			if(type.isArray()) return type.getComponentType();
			else return getClass(generic);
		}

		private Class<?> getClass(Type type) {
			if(type instanceof Class<?> c) return c;
			else if(type instanceof GenericArrayType g) return getClass(g.getGenericComponentType());
			else if(type instanceof ParameterizedType p) return getClass(p.getActualTypeArguments()[0]);
			throw new IllegalArgumentException();
		}

		@SuppressWarnings("unchecked")
		private <C> Collection<C> createCollection(Class<?> type, Class<?> component, List<C> array) {
			if(type.isAssignableFrom(List.class)) return array;
			else if(type.isAssignableFrom(Set.class)) return new HashSet<>(array);
			else if(type.isAssignableFrom(EnumSet.class)) return (Collection<C>) createEnumSet(array, component);

			throw new IllegalStateException("Cannot create collection for" + type.getTypeName() + " with component " + component.getTypeName());
		}

		@SuppressWarnings("unchecked")
		private <E extends Enum<E>> EnumSet<E> createEnumSet(Collection<?> collection, Class<?> component) {
			return collection.isEmpty() ? EnumSet.noneOf((Class<E>) component) : EnumSet.copyOf((Collection<E>) collection);
		}
	};
}
