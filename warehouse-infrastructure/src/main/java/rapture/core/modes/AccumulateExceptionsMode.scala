package rapture.core.modes

import rapture.core.{MethodConstraint, Mode}

import scala.collection.mutable

/**
  * Created by michal on 14.06.2015.
  */
class AccumulateExceptionsMode[+G <: MethodConstraint](implicit var exceptions: mutable.MutableList[Exception]) extends Mode[G] {
  type Wrap[+T, E <: Exception] = T

  override def wrap[T, E <: Exception](t: => T): T =
    try t catch {
      case e: Exception => exceptions += e; null.asInstanceOf[T]
    }

  override def unwrap[Res](value: => Wrap[Res, _ <: scala.Exception]): Res = Option[Res](value).get

  override def toString = "[modes.AccumulateExceptions]"

}


object accumulateExceptions {
  implicit def modeImplicit[G <: MethodConstraint](implicit exceptions: mutable.MutableList[Exception]) =
    new AccumulateExceptionsMode[G]

  def apply[G <: MethodConstraint](implicit exceptions: mutable.MutableList[Exception]) = modeImplicit[G]
}

