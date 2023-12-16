![[Java CI]](https://github.com/Utils4J/JavaUtils/actions/workflows/check.yml/badge.svg)
![[Latest Version]](https://maven.mineking.dev/api/badge/latest/releases/de/mineking/JavaUtils?prefix=v&name=Latest%20Version&color=0374b5)

# Installation

JavaUtils is hosted on a custom repository at [https://maven.mineking.dev](https://maven.mineking.dev/#/releases/de/mineking/JavaUtils). Replace VERSION with the lastest version (without the `v` prefix).
Alternatively, you can download the artifacts from jitpack (not recommended).

### Gradle

```groovy
repositories {
    maven { url "https://maven.mineking.dev/releases" }
}

dependencies {
    implementation "de.mineking:JavaUtils:VERSION"
}
```

### Maven

```xml

<repositories>
    <repository>
        <id>mineking</id>
        <url>https://maven.mineking.dev/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.mineking</groupId>
        <artifactId>JavaUtils</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

# ID

JavaUtils provides a simple id system that allows you to generate id's that are guaranteed to be unique, based on the current time.
You can generate such an id by using:

```java
var id = ID.generate();
System.out.

println(id.asString());
```

To extract the timestamp out of an exsiting id you can use

```java
var id = ID.decode("...");
System.out.

println(id.getTimeCreated());
```

# Database

JavaUtils provides a database system that simplified the process of saving java objects in databases. It is tested with PostgreSQL als database backend but others might work as well.

### Example 1

```java
public class User {
  @Column(key = true)
  public ID id; //You can use the JavaUtils ID class as column. It will automatically be generated. For custom types see TypeMapper and DatabaseManager#addMapper

  @Column
  public String name;

  @Column
  public String email;

  public User() {}

  public User(String name, String email) {
    this.name = name;
    this.email = email;
  }
}

public class Main {
  public static void main(String[] args) {
    DatabaseManager manager = new DatabaseManager("localhost:5432/test", "user", "password");
    Table<User> table = manager.getTable(User.class, User::new, "users");

    var user = new User("Rick Astley", "rick@example.com");
    table.insert(user);
    System.out.println(user.id.asString()); //The id has been generated automatically
  }
}
```

### Example 2

```java
public class Book implements DataClass<Book> {
  @Column(autoincrement = true, key = true)
  public int id;

  @Column
  public String title;

  @Column
  public String author;

  private final Table<Book> table;

  public Book(Table<Book> table) {}

  public Book(Table<Book> table, String title, String author) {
    this.title = title;
    this.author = author;
    this.table = table;
  }

  @Override
  public String toString() {
    return id + ": " + title + " by " + author;
  }
}

public class Main {
  public final DatabaseManager manager;
  public final Table<Book> table;

  public static void main(String[] args) {
    new Main();
  }

  public Main() {
    manager = new DatabaseManager("localhost:5432/test", "user", "password");
    table = manager.getTable(Book.class, this::createInstance, "books");

    System.out.println(new Book(table, "My Life", "Rick Astley").update());
    System.out.println(table.selectAll(Order.ascendingBy("id").limit(5)));

    table.selectMany(Where.greateOrEqual("id", 3)).forEach(Book::delete);
  }

  private Book createInstance() {
    return new Book(table);
  }
}
```

### Example 3

```java
public interface Bookshelf extends Table<Book> { //You can create custom tables
  default List<Book> getByAuthor(String author) {
    return selectMany(Where.equals("author", author));
  }
}

public class Main {
  public static void main(String[] args) {
    DatabaseManager manager = new DatabaseManager("localhost:5432/test", "user", "password");
    Bookshelf table = manager.getTable(Bookshelf.class, User.class, User::new, "books");

    System.out.println(table.getByAuthor("Rick Astley"));
  }
}
```