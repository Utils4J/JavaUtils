package de.mineking.javautils.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class BufferUtils {

	private BufferUtils() {}

	public static ByteBuffer wrapFile(String name) throws IOException {
		return ByteBuffer.wrap(FileUtils.readBytes(name));
	}

	public static ByteBuffer wrapFile(File file) throws IOException {
		return ByteBuffer.wrap(FileUtils.readBytes(file));
	}

	public static void saveBuffer(File file, ByteBuffer buffer) throws IOException {
		FileUtils.saveBytes(file, buffer.array());
	}

	public static void saveBuffer(String name, ByteBuffer buffer) throws IOException {
		FileUtils.saveBytes(name, buffer.array());
	}
}
