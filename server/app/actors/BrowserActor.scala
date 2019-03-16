package actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import ge.zgharbi.shopping.shared.CartEvent
import io.circe.generic.auto._
import io.circe.parser.decode

object BrowserActor {
  def props(manager: ActorRef): Props = Props(new BrowserActor(manager))
}

class BrowserActor(manager: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: String =>
      log.info(s"Received JSON $msg")
      decode[CartEvent](msg) match {
        case Right(evt) =>
          log.info(s"Gog $evt")
          manager forward evt
        case Left(e) =>
          log.error(s"Error processing $msg: $e")
      }
  }
}
