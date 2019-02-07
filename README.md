# Conquerer

A little Civilization wanna-be 4X RTS game.

To get the source, clone this repo using Git:

`git clone https://github.com/cjburkey01/conquerer.git`

This project is distributed with the Gradle wrapper, so only an installation of the JDK (version 11+) is required to run or build versions of **Conquerer**.

### Play it!

Run the Gradle profile `runConquerer`:

* Windows: `gradlew runConquerer`
* Mac/Linux: `./gradlew runConquerer`

### Build it!

If you want to build the game in a self-contained executable file for your operating system, you will execute the `jlink` profile:

* Windows: `gradlew jlink`
* Mac/Linux: `./gradlew jlink`

You will then find the executable for your current operating system in the `build/image/bin` directory.
