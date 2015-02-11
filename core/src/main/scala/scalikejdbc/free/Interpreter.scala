package scalikejdbc.free

import java.sql.SQLException

import scalikejdbc._
import scalikejdbc.free.Query._

import scalaz._
import scalikejdbc.free.Query.GetSeq
import scalikejdbc.free.Query.GenerateKey
import scalikejdbc.free.Query.Update
import scalikejdbc.free.Query.Execute
import scalikejdbc.free.Query.Fold
import scalikejdbc.free.Query.GetOption

abstract class Interpreter[M[_]](implicit M: Monad[M]) extends (Query ~> M) {

  protected def exec[A]: (DBSession => A) => M[A]

  def apply[A](c: Query[A]): M[A] = c match {
    case GetSeq(sql)        => exec(implicit s => sql.asInstanceOf[SQLToList[A, HasExtractor]].apply())   // TODO:
    case GetOption(sql)     => exec(implicit s => sql.asInstanceOf[SQLToOption[A, HasExtractor]].apply())   // TODO:
    case Fold(sql, init, f) => exec(implicit s => sql.foldLeft(init)(f))
    case Execute(sql)       => exec(implicit s => sql.apply())
    case Update(sql)        => exec(implicit s => sql.apply())
    case GenerateKey(sql)   => exec(implicit s => sql.apply())
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
