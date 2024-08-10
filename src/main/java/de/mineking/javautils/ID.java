package de.mineking.javautils;

import io.seruco.encoding.base62.Base62;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

public class ID {
	public final static Base62 BASE62 = Base62.createInstance();
	public final static Charset CHARSET = StandardCharsets.UTF_8;

	public final static int SIZE = Long.BYTES + Byte.BYTES; //Timestamp + Serial

	private static long lastTime = 0;
	private static byte serial;

	@NotNull
	private static String toString(@NotNull byte[] bytes) {
		return new String(BASE62.encode(bytes), CHARSET);
	}

	@NotNull
	private static byte[] fromString(@NotNull String str) {
		return BASE62.decode(str.getBytes(CHARSET));
	}

	@NotNull
	public synchronized static ID generate() {
		var time = System.currentTimeMillis();
		var buffer = ByteBuffer.allocate(SIZE);

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
	public static ID decode(long id) {
		return new ID(BigInteger.valueOf(id).toByteArray());
	}

	@NotNull
	public static ID decode(@NotNull byte[] data) {
		return new ID(data);
	}

	private final byte[] data;

	ID(byte[] data) {
		if(data.length >= SIZE) this.data = data;
		else {
			this.data = new byte[SIZE];
			System.arraycopy(data, 0, this.data, SIZE - data.length, data.length);
		}
	}

	@NotNull
	public String asString() {
		return toString(data).substring(2); //Remove 2 leading zeros
	}

	@NotNull
	public long asNumber() {
		return new BigInteger(data).longValue();
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
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	@Override
	public String toString() {
		return asString();
	}
}
