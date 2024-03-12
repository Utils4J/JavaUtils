package database;

import de.mineking.javautils.ID;
import de.mineking.javautils.database.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WhereTest {
	private final DatabaseManager manager;
	private final Table<TestClass> table;

	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	private class TestClass implements DataClass<TestClass> {
		@Column(key = true)
		public ID id;

		@Column
		public String test;

		@NotNull
		@Override
		public Table<TestClass> getTable() {
			return table;
		}
	}

	public WhereTest() {
		manager = new DatabaseManager("jdbc:postgresql://localhost:5433/postgres", "postgres", "postgres");
		table = manager.getTable(TestClass.class, TestClass::new, "whereTest").createTable();

		manager.getDriver().setSqlLogger(new SqlLogger() {
			@Override
			public void logBeforeExecution(StatementContext context) {
				System.out.println(context.getParsedSql().getSql());
				System.out.println(context.getBinding());
			}
		});

		table.deleteAll();

		table.insert(new TestClass(null, "a"));
		table.insert(new TestClass(null, "a"));
		table.insert(new TestClass(null, "b"));
		table.insert(new TestClass(null, "b"));
		table.insert(new TestClass(null, "c"));
		table.insert(new TestClass(null, "d"));
		table.insert(new TestClass(null, "e"));
	}

	@Test
	public void in() {
		assertEquals(0, table.selectMany(Where.in("test", List.of())).size());
		assertEquals(2, table.selectMany(Where.in("test", List.of("a"))).size());
		assertEquals(2, table.selectMany(Where.in("test", List.of("b"))).size());
		assertEquals(4, table.selectMany(Where.in("test", List.of("a", "b"))).size());
		assertEquals(5, table.selectMany(Where.in("test", List.of("a", "b", "c"))).size());
	}

	@Test
	public void between() {
		assertEquals(2, table.selectMany(Where.between("test", "a", "a")).size());
		assertEquals(5, table.selectMany(Where.between("test", "a", "c")).size());
		assertEquals(3, table.selectMany(Where.between("test", "c", "e")).size());
	}
}
