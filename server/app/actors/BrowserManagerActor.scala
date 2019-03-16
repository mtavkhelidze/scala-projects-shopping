package actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Terminated }
import ge.zgharbi.shopping.shared.{ Alarm, CartEvent }
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable.ListBuffer

private class BrowserManagerActor extends Actor with ActorLogging {
  val clients: ListBuffer[ActorRef] = ListBuffer.empty[ActorRef]

  import BrowserManagerActor._

  override def receive: Receive = {
    case AddBrowser(b) =>
      context.watch(b)
      clients += b
      log.info(s"Add websocket ${b.path}")

    case CartEvent(user, product, action) =>
      val msg = s"The user `$user` ${action.toString} ${product.name}"
      log.info(s"Send alarm `${msg}` to all clients")
      clients.foreach(_ ! Alarm(msg, action).asJson.noSpaces)

    case Terminated(b) =>
      clients -= b
      log.info(s"Close websocket ${b.path}")
  }


}

object BrowserManagerActor {
  def props() = Props(new BrowserManagerActor())

  case class AddBrowser(browser: ActorRef)

}
