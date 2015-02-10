package scalikejdbc.free

import java.sql.SQLException

import scalikejdbc.{DBSession, NoConnectionPoolContext}
import scalikejdbc.free.Query._

import scalaz._

abstract class Interpreter[M[_]](implicit M: Monad[M]) extends (Query ~> M) {

  protected def exec[A]: (DBSession => A) => M[A]

  def apply[A](c: Query[A]): M[A] = c match {
    case GetSeq(sql)                 => exec(s => sql.apply()(s, NoConnectionPoolContext, null))   // TODO:
    case GetFirst(sql)               => exec(s => sql.apply()(s, NoConnectionPoolContext, null))   // TODO:
    case GetSingle(sql)              => exec(s => sql.apply()(s, NoConnectionPoolContext, null))   // TODO:
    case Execute(sql)                => exec(sql.apply()(_))
    case Update(sql)                 => exec(sql.apply()(_))
    case UpdateWithGeneratedKey(sql) => exec(sql.apply()(_))
  }

}


object Interpreter {

  type Executor[A] = Reader[DBSession, A]
  lazy val base = new Interpreter[Executor] {
    protected def exec[A] = Reader.apply
  }

  type SQLEither[A] = SQLException \/ A
  type SafeExecutor[A] = ReaderT[SQLEither, DBSession, A]
  lazy val safe = new Interpreter[SafeExecutor] {
    protected def exec[A] = { f =>
      Kleisli.kleisliU { s: DBSession => \/.fromTryCatchThrowable[A, SQLException](f(s)) }
    }
  }

//  type AsyncExecutor[A] = ReaderT[Future, DBSession, A]
//  def async(implicit ec: ExecutionContext) = new Interpreter[AsyncExecutor] {
//    protected def exec[A]: (DBSession => A) => AsyncExecutor[A] = {
//      f => Kleisli.kleisli(s => Future(f(s)))
//    }
//  }

}
