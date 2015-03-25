package scalikejdbc.free

import java.sql.SQLException

import scalikejdbc._

import scalaz._
import Scalaz._
import scalaz.Free._
import scalikejdbc.free.Query.GetSeq
import scalaz.-\/
import scalikejdbc.free.Query.GenerateKey
import scalikejdbc.free.Query.Update
import scalikejdbc.free.Query.Execute
import scalikejdbc.free.Query.Fold
import scalaz.\/-
import scalikejdbc.free.Query.GetOption

abstract class Interpreter[M[_]](implicit M: Monad[M]) extends (Query ~> M) {

  protected def exec[A](f: DBSession => A): M[A]

  def apply[A](c: Query[A]): M[A] = c match {
    case GetSeq(sql)        => exec(implicit s => sql.apply[Vector]())
    case GetOption(sql)     => exec(implicit s => sql.apply())
    case Fold(sql, init, f) => exec(implicit s => sql.foldLeft(init)(f))
    case Execute(sql)       => exec(implicit s => sql.apply())
    case Update(sql)        => exec(implicit s => sql.apply())
    case GenerateKey(sql)   => exec(implicit s => sql.apply())
  }

  def run[A](q: FreeC[Query, A]): M[A] = Free.runFC(q)(this)

}

object Interpreter {

  lazy val auto = new Interpreter[Id] {
    protected def exec[A](f: DBSession => A) = f(AutoSession)
  }

  type SQLEither[A] = SQLException \/ A
  object SQLEither {
    implicit def TxBoundary[A] = new TxBoundary[SQLEither[A]] {
      def finishTx(result: SQLEither[A], tx: Tx) = {
        result match {
          case \/-(_) => tx.commit()
          case -\/(_) => tx.rollback()
        }
        result
      }
    }
  }
  lazy val safe = new Interpreter[SQLEither] {
    protected def exec[A](f: DBSession => A) = \/.fromTryCatchThrowable[A, SQLException](f(AutoSession))
  }

  type TxExecutor[A] = Reader[DBSession, A]
  lazy val transaction = new Interpreter[TxExecutor] {
    protected def exec[A](f: DBSession => A) = Reader.apply(f)
  }

  type SafeExecutor[A] = ReaderT[SQLEither, DBSession, A]
  lazy val safeTransaction = new Interpreter[SafeExecutor] {
    protected def exec[A](f: DBSession => A) = {
      Kleisli.kleisliU { s: DBSession => \/.fromTryCatchThrowable[A, SQLException](f(s)) }
    }
  }

  case class TesterBuffer(input: Seq[Any], output: Seq[(String, Seq[Any])] = Vector())
  type Tester[A] = State[TesterBuffer, A]
  lazy val tester = new Interpreter[Tester] {
    protected def exec[A](f: DBSession => A) = ???

    override def apply[A](c: Query[A]): Tester[A] = {
      State[TesterBuffer, A] { case TesterBuffer(head +: tail, output) =>
        TesterBuffer(tail, output :+ (c.statement -> c.parameters)) -> head.asInstanceOf[A]
      }
    }

  }

//  type AsyncExecutor[A] = ReaderT[Future, DBSession, A]
//  def async(implicit ec: ExecutionContext) = new Interpreter[AsyncExecutor] {
//    protected def exec[A]: (DBSession => A) => AsyncExecutor[A] = {
//      f => Kleisli.kleisli(s => Future(f(s)))
//    }
//  }

}

