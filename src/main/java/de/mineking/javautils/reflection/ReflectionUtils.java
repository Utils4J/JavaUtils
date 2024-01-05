package de.mineking.javautils.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class ReflectionUtils {
	@NotNull
	public static Class<?> getClass(@NotNull Type type) {
		if(type instanceof Class<?> c) return c;
		else if(type instanceof ParameterizedType pt) return getClass(pt.getRawType());
		else if(type instanceof GenericArrayType) return ReflectionUtils.class;

		throw new IllegalArgumentException("Cannot get class for type '" + type + "'");
	}

	public static boolean isArray(@NotNull Type type, boolean includeCollection) {
		var clazz = getClass(type);
		return type instanceof GenericArrayType || clazz.isArray() || (includeCollection && Collection.class.isAssignableFrom(clazz));
	}

	@NotNull
	public static Optional<Enum<?>> getEnumConstant(@NotNull Type type, @NotNull String name) {
		return Arrays.stream((Enum<?>[]) getClass(type).getEnumConstants())
				.filter(c -> c.name().equals(name))
				.findFirst();
	}

	@NotNull
	public static Type getComponentType(@NotNull Type type) {
		if(type instanceof Class<?> c && c.isArray()) return c.getComponentType();
		else if(type instanceof ParameterizedType pt) return pt.getActualTypeArguments()[0];
		else if(type instanceof GenericArrayType at) return at.getGenericComponentType();

		throw new IllegalArgumentException("Cannot get component for '" + type + "'");
	}

	@NotNull
	public static Type getActualArrayComponent(@NotNull Type type) {
		if(type instanceof Class<?> c) {
			if(c.isArray()) return c.getComponentType();
			else return c;
		} else if(type instanceof GenericArrayType at) return getActualArrayComponent(at.getGenericComponentType());
		else if(type instanceof ParameterizedType pt && Collection.class.isAssignableFrom(getClass(type))) return getActualArrayComponent(pt.getActualTypeArguments()[0]);

		throw new IllegalArgumentException();
	}

	@NotNull
	public static Object[] createArray(@NotNull Type component, int... dimensions) {
		return (Object[]) Array.newInstance(getClass(component), dimensions);
	}

	@NotNull
	public static Stream<?> stream(@NotNull Object o) {
		return o instanceof Collection<?> c ? c.stream() : Arrays.stream((Object[]) o);
	}
}
