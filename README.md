# xtn5250

This is a fork of [xtn5250 emulator](https://sourceforge.net/projects/xtn5250/) which allows connecting to emulate TN5250 terminals.

## Usage

To run the emulator in GUI mode just run the generated jar with something like `java -jar xtn5250.jar` (requires [JRE8+](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html))

Download the library from [releases](https://github.com/blazemeter/xtn5250/releases) and include it in your project (either including on IDE, or in your build tool).

An example usage can be found in [TerminalClientTest](src/test/java/net/infordata/em/TerminalClientTest.java).

## Build

To build the project is required [JDK8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), [maven](https://maven.apache.org/) 3.3+.

Then just run `mvn clean install` and the library will be built and installed in the local maven repository.

## Release

To release the project, define the version to be released by checking included changes since last release and following [semantic versioning](https://semver.org/). 
Then, create a [release](https://github.com/blazemeter/xtn5250/releases) (including `v` as prefix, e.g. `v0.1`), this will trigger a Travis build which will publish the jars to the created github release.
