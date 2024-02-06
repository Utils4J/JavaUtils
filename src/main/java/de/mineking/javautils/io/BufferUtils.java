package de.mineking.javautils.io;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class BufferUtils {

	private BufferUtils() { }

	@NotNull
	public static ByteBuffer wrapFile(@NotNull String name) throws IOException {
		return ByteBuffer.wrap(FileUtils.readBytes(name));
	}

	@NotNull
	public static ByteBuffer wrapFile(@NotNull File file) throws IOException {
		return ByteBuffer.wrap(FileUtils.readBytes(file));
	}

	public static void saveBuffer(@NotNull File file, @NotNull ByteBuffer buffer) throws IOException {
		FileUtils.saveBytes(file, buffer.array());
	}

	public static void saveBuffer(@NotNull String name, @NotNull ByteBuffer buffer) throws IOException {
		FileUtils.saveBytes(name, buffer.array());
	}
}
