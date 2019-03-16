package controllers

import actors.{ BrowserActor, BrowserManagerActor }
import akka.actor.{ ActorLogging, ActorRef, ActorSystem }
import akka.stream.Materializer
import javax.inject._
import play.api.libs.streams.ActorFlow
import play.api.mvc.{ AbstractController, ControllerComponents, WebSocket }
import play.api.mvc

@Singleton
class WebSockets @Inject()(
    implicit ac: ActorSystem,
    materializer: Materializer,
    cc: ControllerComponents
) extends AbstractController(cc) with ActorLogging {

  val managerActor: ActorRef = ac
      .actorOf(BrowserManagerActor.props(), "manager-actor")

  def cartEventsWS: mvc.WebSocket =
    WebSocket.accept[String, String] { implicit req =>
      ActorFlow.actorRef { out =>
        log.info(s"Connection from ${req.host}")
        managerActor ! BrowserManagerActor.AddBrowser(out)
        BrowserActor.props(managerActor)
      }
    }
}
