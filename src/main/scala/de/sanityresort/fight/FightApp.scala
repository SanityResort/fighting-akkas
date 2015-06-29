package de.sanityresort.fight

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import de.heikoseeberger.akkasse.{EventStreamMarshalling, ServerSentEvent}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.language.implicitConversions

object FightApp {
  def main(args: Array[String]): Unit = {
    new FightApp()
  }

}


class FightApp() extends EventStreamMarshalling {

  import FightController._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  implicit val system = ActorSystem("fight-system")

  import system.dispatcher

  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(5 seconds)


  val controller: ActorRef = system.actorOf(FightController.props)

  Http().bindAndHandle(routes, interface = "localhost", port = 9999)

  StdIn.readLine("Hit ENTER to exit ...")
  system.shutdown()
  system.awaitTermination()


  def routes(implicit ec: ExecutionContext, mat: Materializer) =
    get {
      pathPrefix("fight" / Segment) { fightName =>
        pathSuffix("create") {
          complete {
            (controller ? Create(fightName)).mapTo[(StatusCode, Result)]
          }
        } ~
          path("start") {
            complete {
              (controller ? Start(fightName)).mapTo[Either[(StatusCode, Result), Source[ServerSentEvent, Unit]]]
            }
          } ~
          pathEnd {
            complete {
              (controller ? Status(fightName)).mapTo[Either[(StatusCode, Result), Source[ServerSentEvent, Unit]]]
            }
          }
      } ~
        path("fights") {
          complete {
            (controller ? List).mapTo[(StatusCode, Results)]
          }
        }

    }
}
