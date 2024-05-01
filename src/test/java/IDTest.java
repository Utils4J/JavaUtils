import de.mineking.javautils.ID;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IDTest {
	private final ID id = ID.generate();
	private final long time = System.currentTimeMillis();

	@Test
	public void time() {
		assertThat(id.getTimeCreated().toEpochMilli()).isCloseTo(time, Offset.offset(1L));
	}

	@Test
	public void decode() {
		assertEquals(id, ID.decode(id.toString()));
		assertEquals(id, ID.decode(id.asString()));
		assertEquals(id, ID.decode(id.asNumber()));
	}

	@Test
	public void format() {
		System.out.println(id);
		System.out.println(id.asString());
		System.out.println(id.asNumber());
	}
}
