package de.sanityresort.fight

import de.heikoseeberger.akkasse.ServerSentEvent
import scala.language.implicitConversions

object State{
  implicit def gameStateToSse(state: State): ServerSentEvent ={
    ServerSentEvent(state.toString)
  }
}

case class State(name: String) {

}
