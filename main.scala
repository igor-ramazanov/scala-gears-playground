//> using scala 3.5.0-RC1

//> using option -deprecation
//> using option -feature
//> using option -new-syntax
//> using option -rewrite
//> using option -unchecked
//> using option -Wnonunit-statement
//> using option -Wsafe-init
//> using option -Wunused:all
//> using option -Wvalue-discard

//> using platform scala-native
//> using nativeLto thin
//> using nativeMode release-fast
//> using nativeVersion 0.5.3
//> using nativeEmbedResources true

//> using resourceDir resources

//> using dep org.typelevel::cats-core::2.12.0
//> using dep ch.epfl.lamp::gears::0.2.0

import gears.async.*
import gears.async.default.given
import java.io as Io
import java.net as Net

object Main:

  final case class Socket(value: Net.Socket) extends Cancellable:

    override def cancel(): Unit =
      try
        println(s"Closing the socket due to cancelling the Async scope: $value")
        value.close()
        println(s"Socket closed due to cancelling the Async scope: $value")
      catch e =>
          println(s"Failed to close the socket during Async scope cancellation: $value, error: ${e.getMessage}")
      finally unlink()

  def main(args: Array[String]): Unit = Async.blocking:
    val serverSocket: Net.ServerSocket = new Net.ServerSocket()
    serverSocket.bind(Net.InetSocketAddress.createUnresolved("localhost", 8080))
    val group = summon[Async.Spawn].group
    while !group.isCancelled do
      println(s"Awaiting for the connection")
      val socket: Socket = Socket(serverSocket.accept())
      socket.link()
      val fut = Future:
        handle(socket)
    println("Not accepting any new connections. The server will be stopped after all clients close their connections.")
    println("All connections closed, stopping the server.")
    try
      serverSocket.close()
      println("Server closed")
    catch
      case e => println(s"An error during stopping the server: ${e.getMessage}")
  end main

  def handle(socket: Socket)(using A: Async): Unit =
    try
      println(s"Got a socket: $socket")
      val reader = new Io.BufferedReader(new Io.InputStreamReader(
        new Io.DataInputStream(socket.value.getInputStream())
      ))
      val writer = new Io.BufferedWriter(new Io.OutputStreamWriter(
        new Io.DataOutputStream(socket.value.getOutputStream())
      ))
      var continue = true
      while continue && !A.group.isCancelled do
        println(s"Waiting for data on the socket")
        Option(reader.readLine()) match
          case None =>
            println("Empty data: closing the socket")
            continue = false
          case Some("stop") =>
            println("Stopping the server")
            continue = false
            A.group.cancel()
          case Some(line) =>
            println(s"Got the data: $line")
            writer.write(s"echo: $line\n")
            writer.flush()
    catch
      case e =>
        println(s"Encountered an error during communication: ${e.getMessage()}")
        e.printStackTrace()
        println(s"Will attempt to close the socket")
        try
          socket.value.close()
          println(s"Socket closed")
        catch case e => println(s"Failed to close the socket: ${e.getMessage}")
  end handle

end Main
