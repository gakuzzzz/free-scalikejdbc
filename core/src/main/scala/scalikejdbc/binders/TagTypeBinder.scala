package scalikejdbc
package binders

import scalaz._

trait TagTypeBinder {
  @inline implicit def tagTypeBinder[A, T](implicit b: TypeBinder[A]): TypeBinder[A @@ T] = Tag.subst[A, TypeBinder, T](b)
}
object tag extends TagTypeBinder
