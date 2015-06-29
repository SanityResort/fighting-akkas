package de.sanityresort.fight

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Props}
import de.heikoseeberger.akkasse.{ServerSentEvent, EventPublisher}
import scala.concurrent.duration.DurationInt
import scala.language.implicitConversions

object Reporter{
  def props:Props = Props(new Reporter)
}

class Reporter extends EventPublisher[String](10, 2.second) with ActorLogging{

  override protected def receiveEvent: Receive = {
    case message:String =>
      onEvent(message)
  }


}
