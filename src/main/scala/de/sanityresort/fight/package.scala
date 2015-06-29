package de.sanityresort

import de.heikoseeberger.akkasse.ServerSentEvent
import scala.language.implicitConversions


package object fight {

  implicit def stringToSse(message: String): ServerSentEvent ={
    ServerSentEvent(message)
  }
}
