import gears.async.*
import gears.async.default.given
import java.io as Io
import java.net as Net

object Main:

  def main(args: Array[String]): Unit = Async.blocking:
    val serverSocket: Net.ServerSocket = new Net.ServerSocket()
    serverSocket.bind(Net.InetSocketAddress.createUnresolved("localhost", 8080))
    val group = summon[Async.Spawn].group
    while !group.isCancelled do
      scribe.info(s"Awaiting for the connection")
      val socket: Socket = Socket(serverSocket.accept())
      socket.link()
      val _ = Future:
        handle(socket)
    scribe.info("Not accepting any new connections. The server will be stopped after all clients close their connections.")
    scribe.info("All connections closed, stopping the server.")
    try
      serverSocket.close()
      scribe.info("Server closed")
    catch
      case e => scribe
          .info(s"An error during stopping the server: ${e.getMessage}")
  end main

  def handle(socket: Socket)(using A: Async): Unit =
    try
      scribe.info(s"Got a socket: $socket")
      val reader = new Io.BufferedReader(new Io.InputStreamReader(
        new Io.DataInputStream(socket.value.getInputStream())
      ))
      val writer = new Io.BufferedWriter(new Io.OutputStreamWriter(
        new Io.DataOutputStream(socket.value.getOutputStream())
      ))
      var continue = true
      while continue && !A.group.isCancelled do
        scribe.info(s"Waiting for data on the socket")
        Option(reader.readLine()) match
          case None =>
            scribe.info("Empty data: closing the socket")
            continue = false
          case Some("stop") =>
            scribe.info("Stopping the server")
            continue = false
            A.group.cancel()
          case Some(line) =>
            scribe.info(s"Got the data: $line")
            writer.write(s"echo: $line\n")
            writer.flush()
    catch
      case e =>
        scribe
          .info(s"Encountered an error during communication: ${e.getMessage()}")
        e.printStackTrace()
        scribe.info(s"Will attempt to close the socket")
        try
          socket.value.close()
          scribe.info(s"Socket closed")
        catch
          case e => scribe.info(s"Failed to close the socket: ${e.getMessage}")
  end handle

end Main
