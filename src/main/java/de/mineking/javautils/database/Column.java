package de.mineking.javautils.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	String name() default "";

	boolean key() default false;

	boolean unique() default false;

	String modifier() default "";

	boolean autoincrement() default false;
}
