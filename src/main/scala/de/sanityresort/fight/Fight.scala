package de.sanityresort.fight

import akka.actor.{ActorRef, Props}
import de.sanityresort.fight.Fight._
import de.sanityresort.fight.Fighter.Strike


object Fight {

  case class Stopped(name: String)

  case class Start(reporter: ActorRef)

  case class AddReporter(reporter: ActorRef)

  case class DefineOpponent(fighter: ActorRef)

  def props(name: String): Props = {
    Props(new Fight(name))
  }

  val Health = 100

}

class Fight(name: String) extends LActor {

  var fighters: Map[ActorRef, Int] = Map().withDefaultValue(Health)

  val fighter1 = context.actorOf(Fighter.props(self), "fighter1")
  val fighter2 = context.actorOf(Fighter.props(self), "fighter2")

  fighter1 ! DefineOpponent(fighter2)
  fighter2 ! DefineOpponent(fighter1)

  var reporters: Seq[ActorRef] = Seq()

  override def receive: Receive = {
    case Strike(damage, opponent) =>
      updateFighter(damage, opponent)
      val health: Int = fighters.get(opponent).get
      sendMessage(s"${opponent.path.name} got hit - Health is at $health")
      if (health <= 0) {
        sendMessage(s"${opponent.path.name} lost!!")
        reporters.foreach(reporter => context.stop(reporter))

        context.parent ! Stopped(name)
        context.stop(self)
      }
    case Start(actor) =>

      reporters = reporters :+ actor
      log.info("Started fight")

      fighter1 ! Fight.Start
      fighter2 ! Fight.Start

    case AddReporter(actor) => {
      reporters = reporters :+ actor

    }
  }

  private def sendMessage(message: String): Unit = {
    log.info(message)
    reporters.foreach(reporter => {
      log.info(s"sending to reporter ${reporter.path.name}")
      reporter ! message
    })
  }

  private def updateFighter(damage: Int, fighter: ActorRef): Unit = {
    fighters += fighter -> (fighters(fighter) - damage)
  }
}
