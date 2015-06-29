package de.sanityresort.fight

import java.lang.ProcessBuilder.Redirect

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorRef, ActorSystem}
import akka.http.scaladsl.model.{StatusCodes, StatusCode}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import de.heikoseeberger.akkasse.ServerSentEvent
import de.sanityresort.fight.Fight.{AddReporter, Stopped}
import spray.json.DefaultJsonProtocol

object FightController {

  case class Start(name: String)

  case class Create(name: String)

  object List

  case class Status(name: String)

  def props: Props = {
    Props(new FightController)
  }
}

class FightController extends LActor {

  import akka.http.scaladsl.model.StatusCodes._
  import FightController._

  private var createdFights: Map[String, ActorRef] = Map()

  private var runningFights: Map[String, ActorRef] = Map()

  private def addFight(name: String): Unit = {
    if (createdFights.contains(name)) {
      sender ! (OK, Result(s"Fight $name already exists"))
    } else {
      createdFights += name -> context.actorOf(Fight.props(name))
      sender ! (Created, Result(s"Fight $name created"))
    }
  }

  private def listFights(): Unit = {
    sender() ! (OK, Results(createdFights.keys))
  }

  private def startFight(name: String): Unit = {
    if (createdFights.contains(name)) {

      val reporter = context.actorOf(Reporter.props)
      val source = Source(ActorPublisher[ServerSentEvent](reporter))
      val fight: ActorRef =  createdFights(name)
      fight ! Fight.Start(reporter)
      runningFights += name ->  fight

      createdFights -= name

      sender() ! Right(source)
    } else if (runningFights.contains(name)) {
      sender() ! Left((Found, Result(s"/fight/$name")))
    }else {
      sender() ! Left((NotFound, Result(s"No fight with name $name found")))
    }
  }

  private def state(name: String): Unit = {

    if (createdFights.contains(name)) {
      sender() ! Left((NotFound, Result(s"Fight $name is not started yet")))
    } else if (runningFights.contains(name)) {
      val reporter = context.actorOf(Reporter.props)
      val source = Source(ActorPublisher[ServerSentEvent](reporter))
      runningFights(name) ! AddReporter(reporter)


      sender() ! Right(source)
    } else {
      sender() ! Left((NotFound, Result(s"No fight with name $name found")))
    }

  }

  private def finish(name: String): Unit = {
    runningFights -= name
  }

  override def receive: Receive = {
    case List => listFights()
    case Start(name) => startFight(name)
    case Create(name) => addFight(name)
    case Status(name) => state(name)
    case Stopped(name) => finish(name)
  }
}

object Result extends DefaultJsonProtocol {
  implicit val resultFormat = jsonFormat1(Result.apply)
}

case class Result(response: String)

object Results extends DefaultJsonProtocol {
  implicit val resultFormat = jsonFormat1(Results.apply)
}

case class Results(response: Iterable[String])

