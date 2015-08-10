package scalikejdbc
package binders

import scalaz.Isomorphism._

trait IsoTypeBinder {
  implicit def isoTypeBinder[A, B](implicit iso: A <=> B, tba: TypeBinder[A]): TypeBinder[B] = tba.map(iso.to)
}
object iso extends IsoTypeBinder