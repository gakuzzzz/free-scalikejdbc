package scalikejdbc.free

import scalikejdbc.NoConnectionPoolContext
import scalikejdbc.free.Query._

import scalaz.~>


object Interpreter extends (Query ~> Executor) {
  def apply[A](c: Query[A]) = c match {
    case GetSeq(sql) => exec(s => sql.apply()(session = s, NoConnectionPoolContext, null))   // TODO:
    case GetFirst(sql) => exec(s => sql.apply()(session = s, NoConnectionPoolContext, null))   // TODO:
    case GetSingle(sql) => exec(s => sql.apply()(session = s, NoConnectionPoolContext, null))   // TODO:
    case Execute(sql) => exec(sql.apply()(_))
    case Update(sql) => exec(sql.apply()(_))
    case UpdateWithGeneratedKey(sql) => exec(sql.apply()(_))
  }
}
