package de.mineking.javautils.io;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileUtils {

	private FileUtils() {}

	public static byte[] readBytes(@NotNull File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return fis.readAllBytes();
		}
	}

	public static byte[] readBytes(@NotNull String name) throws IOException {
		try (FileInputStream fis = new FileInputStream(name)) {
			return fis.readAllBytes();
		}
	}

	public static void saveBytes(@NotNull File file, byte[] data) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(data);
		}
	}

	public static void saveBytes(@NotNull String name, byte[] data) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(name)) {
			fos.write(data);
		}
	}

	@NotNull
	public static String getExtension(@NotNull File file) {
		return getExtension(file.getName());
	}

	@NotNull
	public static String getExtension(@NotNull String name) {
		while (name.endsWith(".")) name = name.substring(0, name.length()-1);
		return name.substring(name.lastIndexOf('.')+1);
	}
}
