package scalikejdbc

import scalaz.Reader


package object free {

  type Executor[A] = Reader[DBSession, A]

  def exec[A](f: DBSession => A): Executor[A] = Reader(f)

}
