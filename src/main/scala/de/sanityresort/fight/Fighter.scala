package de.sanityresort.fight

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Props}
import de.sanityresort.fight.Fighter._

import scala.concurrent.duration.FiniteDuration
import scala.math._

object Fighter {

  object Strike

  object Start

  case class Strike(damage: Int, opponent: ActorRef)

  def props(fight: ActorRef): Props = Props(new Fighter(fight))

}

case class Fighter(fight: ActorRef) extends LActor {

  import context.dispatcher

  var opponent: Option[ActorRef] = None


  override def receive: Receive = {
    case Strike =>
      fight ! Strike(calculateDamage(), opponent.get)
      scheduleStrike()
    case Fight.Start =>
      scheduleStrike()

    case Fight.DefineOpponent(fighter) => opponent = Some(fighter)
  }

  private def scheduleStrike(): Unit = {
    context.system.scheduler.scheduleOnce(
      calculateNextDelay(), self, Strike)

  }

  private def calculateDamage(): Int = {
    {
      5 + (random - 0.5) * 8
    }.toInt
  }

  private def calculateNextDelay(): FiniteDuration = {
    FiniteDuration((random * 1000).toLong, TimeUnit.MILLISECONDS)
  }
}
