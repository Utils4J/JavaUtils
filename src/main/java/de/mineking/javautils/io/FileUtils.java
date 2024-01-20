package de.mineking.javautils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileUtils {

	private FileUtils() {}

	public static byte[] readBytes(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return fis.readAllBytes();
		}
	}

	public static byte[] readBytes(String name) throws IOException {
		try (FileInputStream fis = new FileInputStream(name)) {
			return fis.readAllBytes();
		}
	}

	public static void saveBytes(File file, byte[] data) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(data);
		}
	}

	public static void saveBytes(String name, byte[] data) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(name)) {
			fos.write(data);
		}
	}

	public static String getExtension(File file) {
		return getExtension(file.getName());
	}

	public static String getExtension(String name) {
		while (name.endsWith(".")) name = name.substring(0, name.length()-1);
		return name.substring(name.lastIndexOf('.')+1);
	}
}
