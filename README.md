# xtn5250

This is a fork of [xtn5250 emulator](https://sourceforge.net/projects/xtn5250/) which allows connecting to emulate TN5250 terminals.

## Usage

To run the emulator in GUI mode just run the generated jar with something like `java -jar xtn5250.jar`.

>Check latest version in [releases](https://github.com/abstracta/xtn5250/releases).

To use the emulator as maven dependency include in `pom.xml`:

```xml
<dependency>
    <groupId>us.abstracta</groupId>
    <artifactId>xtn5250</artifactId>
    <version>1.20</version>
</dependency>
```

>Check latest version in [releases](https://github.com/abstracta/xtn5250/releases).

An example usage can be found in [TerminalClientTest](src/test/java/net/infordata/em/TerminalClientTest.java).

## Build

To build the project is required [JDK8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), [maven](https://maven.apache.org/) 3.3+.

Then just run `mvn clean install` and the library will be built and installed in the local maven repository.

## Release

To release the project, define the version to be released by checking included changes since last release and following [semantic versioning](https://semver.org/). 
Then, create a [release](https://github.com/abstracta/xtn5250/releases) (including `v` as prefix, e.g. `v0.1`), this will trigger a Travis build which will publish the jars to maven central repository (and make it general available to be used as maven dependency projects) in around 10 mins and can be found in [maven central search](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22us.abstracta%22%20AND%20a%3A%22xtn5250%22) after up to 2 hours.
