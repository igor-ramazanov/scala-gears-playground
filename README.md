# Scala Gears playground
A playground to test the direct effects style with Scala Native 3 and an experimental asynchronous programming [Gears](https://lampepfl.github.io/gears) library.

It's an attempt to build a simple TCP echo server and explore topics such as:
* concurrency
* cancellations
* timers
* error handling

The app is built with the help of an awesome [ScalaCLI](https://scala-cli.virtuslab.org) and [Nix devshell](https://github.com/numtide/devshell) which provides a development environment.

I didn't try to make the development environment completely self-contained as I keep [ScalaCLI](https://scala-cli.virtuslab.org), JDK and [Metals](https://scalameta.org/metals/) globally installed,
but that may change in the future.
