package de.mineking.javautils.database;

import de.mineking.javautils.ID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public interface TypeMapper<T, R> {
	boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f);

	@NotNull
	String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f);

	@SuppressWarnings("unchecked")
	@Nullable
	default T value(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable R value) {
		return (T) value;
	}

	@Nullable
	T extract(@NotNull ResultSet set, @NotNull String name) throws SQLException;

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
		public Integer extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
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
		public Integer extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
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
		public Long extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
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
		public Double extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
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
		public Boolean extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
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
		public String extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
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
		public Instant extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
			var timestamp = set.getTimestamp(name);
			return timestamp == null ? null : timestamp.toInstant();
		}
	};

	TypeMapper<String, ID> ID = new TypeMapper<>() {
		@Override
		public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return de.mineking.javautils.ID.class.isAssignableFrom(type);
		}

		@NotNull
		@Override
		public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f) {
			return "text";
		}

		@NotNull
		@Override
		public String value(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable de.mineking.javautils.ID value) {
			return value == null ? de.mineking.javautils.ID.generate().asString() : value.asString();
		}

		@Nullable
		@Override
		public String extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
			return set.getString(name);
		}

		@NotNull
		@Override
		public de.mineking.javautils.ID parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @NotNull String value) {
			return de.mineking.javautils.ID.decode(value);
		}
	};

	TypeMapper<Object, Optional<?>> OPTIONAL = new TypeMapper<>() {
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

		@Nullable
		@Override
		public Object value(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Optional<?> value) {
			return value == null ? null : value.orElse(null);
		}

		@Nullable
		@Override
		public Object extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
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

		@Nullable
		@Override
		public String value(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Enum<?> value) {
			return value == null ? null : value.name();
		}

		@Nullable
		@Override
		public String extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
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

		@Nullable
		@Override
		public Object[] value(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable Object value) {
			if(value == null) return null;
			var ct = getComponentType(type, f.getGenericType());
			var temp = type.isArray() ? Arrays.asList((Object[]) value) : (Collection<?>) value;
			var array = temp.stream()
					.map(t -> manager.value(ct, f, t))
					.toArray();

			try {
				return getArray(manager.db.withHandle(handle -> handle.getConnection().createArrayOf(manager.getType(ct, f), array)));
			} catch(SQLException e) {
				throw new RuntimeException(e);
			}
		}

		@Nullable
		@Override
		public Object[] extract(@NotNull ResultSet set, @NotNull String name) throws SQLException {
			return (Object[]) set.getArray(name).getArray();
		}

		@Nullable
		@Override
		public Object parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable Object[] value) {
			if(value == null) return null;

			var component = getComponentType(type, field.getGenericType());

			var array = Arrays.stream(value)
					.map(e -> manager.parse(component, field, e))
					.toList();

			return type.isArray() ? array.toArray() : createCollection(type, component, array);
		}

		private Class<?> getComponentType(Class<?> type, Type generic) {
			if(type.isArray()) return type.getComponentType();
			else {
				var p = ((ParameterizedType) generic).getActualTypeArguments()[0];
				return (Class<?>) p;
			}
		}

		@SuppressWarnings("unchecked")
		private <C> Collection<C> createCollection(Class<?> type, Class<?> component, List<C> array) {
			if(type.isAssignableFrom(List.class)) return array;
			else if(type.isAssignableFrom(Set.class)) return new HashSet<>(array);
			else if(type.isAssignableFrom(EnumSet.class)) return (Collection<C>) createEnumSet(array, component);

			throw new IllegalStateException();
		}

		@SuppressWarnings("unchecked")
		private <E extends Enum<E>> EnumSet<E> createEnumSet(Collection<?> collection, Class<?> component) {
			return collection.isEmpty() ? EnumSet.noneOf((Class<E>) component) : EnumSet.copyOf((Collection<E>) collection);
		}

		@SuppressWarnings("unchecked")
		private <C> C[] getArray(Array array) throws SQLException {
			return (C[]) array.getArray();
		}
	};
}
