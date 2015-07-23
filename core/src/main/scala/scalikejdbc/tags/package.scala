package scalikejdbc

import scalaz._

package object tags {

  @inline implicit def tagTypeBinder[A, T](implicit b: TypeBinder[A]): TypeBinder[A @@ T] = Tag.subst[A, TypeBinder, T](b)

}
