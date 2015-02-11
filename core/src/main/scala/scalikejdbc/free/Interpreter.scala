package scalikejdbc.free

import java.sql.SQLException

import scalikejdbc._
import scalikejdbc.free.Query._

import scalaz._
import Scalaz._

abstract class Interpreter[M[_]](implicit M: Monad[M]) extends (Query ~> M) {

  protected def exec[A]: (DBSession => A) => M[A]

  def apply[A](c: Query[A]): M[A] = c match {
    case GetSeq(sql)        => exec(implicit s => sql.apply())   // TODO:
    case GetOption(sql)     => exec(implicit s => sql.apply())   // TODO:
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

  type StatementWriter[A] = Writer[List[(String, Seq[Any])], A]
  type Tester[A] = StateT[StatementWriter, Seq[Any], A]
  lazy val tester = new Interpreter[Tester] {
    protected def exec[A] = ???

    private def test[A](statement: String, parameters: Seq[Any]): Tester[A] = {
      StateT[StatementWriter, Seq[Any], A] { input =>
        Writer(List(statement -> parameters), input.tail -> input.head.asInstanceOf[A])
      }
    }

    override def apply[A](c: Query[A]): Tester[A] = c match {
      case GetSeq(sql)      => test(sql.statement, sql.parameters)
      case GetOption(sql)   => test(sql.statement, sql.parameters)
      case Fold(sql, _, _)  => test(sql.statement, sql.parameters)
      case Execute(sql)     => test(sql.statement, sql.parameters)
      case Update(sql)      => test(sql.statement, sql.parameters)
      case GenerateKey(sql) => test(sql.statement, sql.parameters)
    }

  }

//  type AsyncExecutor[A] = ReaderT[Future, DBSession, A]
//  def async(implicit ec: ExecutionContext) = new Interpreter[AsyncExecutor] {
//    protected def exec[A]: (DBSession => A) => AsyncExecutor[A] = {
//      f => Kleisli.kleisli(s => Future(f(s)))
//    }
//  }

}
