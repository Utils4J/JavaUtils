package database;

import de.mineking.javautils.ID;
import de.mineking.javautils.database.*;
import de.mineking.javautils.database.exception.ConflictException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateTest {
	private final DatabaseManager manager;
	private final Table<TestClass> table;

	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	private class TestClass implements DataClass<TestClass> {
		@Column(key = true)
		public ID id;

		@Column(unique = true)
		public String test;

		@Column(unique = true)
		public int x;

		@NotNull
		@Override
		public Table<TestClass> getTable() {
			return table;
		}
	}

	public UpdateTest() {
		manager = new DatabaseManager("jdbc:postgresql://localhost:5433/postgres", "postgres", "postgres");
		table = manager.getTable(TestClass.class, TestClass::new, "update").createTable();

		manager.getDriver().setSqlLogger(new SqlLogger() {
			@Override
			public void logBeforeExecution(StatementContext context) {
				System.out.println(context.getParsedSql().getSql());
				System.out.println(context.getBinding());
			}
		});
	}

	@BeforeEach
	public void reset() {
		table.deleteAll();
	}

	@Test
	public void update() {
		var test = new TestClass();
		test.insert();

		assertNull(table.selectOne(Where.equals("id", test.id)).get().test);

		test.test = "abc";
		test.update();
		assertEquals(table.selectOne(Where.equals("id", test.id)).get().test, "abc");
	}

	@Test
	public void updateConflict() {
		var test1 = new TestClass();
		test1.insert();

		var test2 = new TestClass();
		assertThrows(ConflictException.class, test2::insert);

		test2.test = "abc";
		test2.x = 5;
		test2.insert();

		test1.update();

		test1.test = "def";
		test1.x = 5;
		assertThrows(ConflictException.class, test1::update);

		test1.x = 10;
		test1.update();
		test1.update();

		test2.x = 10;
		assertThrows(ConflictException.class, test2::update);

		test2.x = 15;
		test2.update();
	}

	@Test
	public void upsert() {
		var test = new TestClass();
		test.upsert();

		test.test = "abc";
		test.upsert();
		assertEquals(table.selectOne(Where.equals("id", test.id)).get().test, "abc");

		var test2 = new TestClass();
		test2.test = "abc";
		assertThrows(ConflictException.class, test2::upsert);
	}
}
