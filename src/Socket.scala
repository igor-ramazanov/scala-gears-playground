import gears.async.Cancellable
import java.net as Net

final case class Socket(value: Net.Socket) extends Cancellable:

  override def cancel(): Unit =
    try
      scribe
        .info(s"Closing the socket due to cancelling the Async scope: $value")
      value.close()
      scribe.info(s"Socket closed due to cancelling the Async scope: $value")
    catch e =>
        scribe.info(s"Failed to close the socket during Async scope cancellation: $value, error: ${e.getMessage}")
    finally super.unlink()
