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