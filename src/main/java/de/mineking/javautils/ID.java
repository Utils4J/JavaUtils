package de.mineking.javautils;

import io.seruco.encoding.base62.Base62;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

public class ID {
	private final static Base62 base62 = Base62.createInstance();
	public final static int size = Long.BYTES + Byte.BYTES; //Timestamp + Serial

	private static long lastTime = 0;
	private static byte serial;

	@NotNull
	private static String toString(@NotNull byte[] bytes) {
		return new String(base62.encode(bytes), StandardCharsets.UTF_8);
	}

	@NotNull
	private static byte[] fromString(@NotNull String str) {
		return base62.decode(str.getBytes(StandardCharsets.UTF_8));
	}

	@NotNull
	public synchronized static ID generate() {
		var time = System.currentTimeMillis();
		var buffer = ByteBuffer.allocate(size);

		if(time > lastTime) serial = Byte.MIN_VALUE;
		else if(serial == Byte.MAX_VALUE) {
			serial = Byte.MIN_VALUE;
			time++;
		}

		buffer.putLong(time);
		buffer.put(serial++);

		lastTime = time;

		return new ID(buffer.array());
	}

	@NotNull
	public static ID decode(@NotNull String id) {
		return new ID(fromString(id));
	}

	@NotNull
	public static ID decode(@NotNull BigInteger id) {
		return new ID(id.toByteArray());
	}

	@NotNull
	public static ID decode(@NotNull byte[] data) {
		return new ID(data);
	}

	private final byte[] data;

	ID(byte[] data) {
		this.data = data;
	}

	@NotNull
	public String asString() {
		return toString(data);
	}

	@NotNull
	public BigInteger asNumber() {
		return new BigInteger(data);
	}

	@NotNull
	public ByteBuffer bytes() {
		return ByteBuffer.wrap(data);
	}

	public Instant getTimeCreated() {
		return Instant.ofEpochMilli(bytes().getLong());
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ID i && Arrays.equals(data, i.data);
	}

	@Override
	public String toString() {
		return asString();
	}
}
