package scalikejdbc
package tagless

import java.sql.SQLException

import scalaz._
import scalaz.Id._
import scalaz.\/._

sealed abstract class ScalikeJDBC[F[_]: Monad] {
  protected def exec[A](f: DBSession => A): F[A]

  def vector[A](sql: SQLBuilder[_])(f: WrappedResultSet => A): F[Vector[A]] = exec(implicit s => withSQL(sql).map(f).toCollection.apply[Vector]())
  def single[A](sql: SQL[A, HasExtractor]): F[Option[A]] = exec(implicit s => sql.single().apply())
  def single[A](sql: SQLBuilder[_])(f: WrappedResultSet => A): F[Option[A]] = single(withSQL(sql).map(f))
  def generateKey(sql: SQLBuilder[UpdateOperation]): F[Long] = exec(implicit s => withSQL(sql).updateAndReturnGeneratedKey().apply())

}

abstract class Interpreter {
  type F[A]

  def vector[A](sql: SQLBuilder[_])(f: WrappedResultSet => A)(implicit F: ScalikeJDBC[F]): F[Vector[A]] = F.vector(sql)(f)
  def single[A](sql: SQL[A, HasExtractor])(implicit F: ScalikeJDBC[F]): F[Option[A]] = F.single(sql)
  def single[A](sql: SQLBuilder[_])(f: WrappedResultSet => A)(implicit F: ScalikeJDBC[F]): F[Option[A]] = F.single(sql)(f)
  def generateKey(sql: SQLBuilder[UpdateOperation])(implicit F: ScalikeJDBC[F]): F[Long] = F.generateKey(sql)

}

object Interpreter {

  private def eitherTxBoundary[A] = new TxBoundary[SQLException \/ A] {
    def finishTx(result: SQLException \/ A, tx: Tx) = {
      result match {
        case \/-(_) => tx.commit()
        case -\/(_) => tx.rollback()
      }
      result
    }
  }

  object auto extends Interpreter {
    type F[A] = Id[A]
    implicit val autoInterpreter = new ScalikeJDBC[F] {
      protected def exec[A](f: DBSession => A): F[A] = f(AutoSession)
    }
  }

  object safe extends Interpreter {
    type F[A] = SQLException \/ A
    implicit def TxBoundary[A]: TxBoundary[F[A]] = eitherTxBoundary
    implicit val safeInterpreter = new ScalikeJDBC[F] {
      protected def exec[A](f: DBSession => A) = \/.fromTryCatchThrowable[A, SQLException](f(AutoSession))
    }
  }

  object transaction extends Interpreter {
    type F[A] = Reader[DBSession, A]
    implicit val txInterpreter = new ScalikeJDBC[F] {
      protected def exec[A](f: DBSession => A) = Reader(f)
    }
  }

  object safeTransaction extends Interpreter {
    type SQLEither[A] = SQLException \/ A
    type F[A] = ReaderT[SQLEither, DBSession, A]
    implicit def TxBoundary[A]: TxBoundary[SQLEither[A]] = eitherTxBoundary
    implicit val txInterpreter = new ScalikeJDBC[F] {
      protected def exec[A](f: DBSession => A) = {
        Kleisli.kleisliU { s: DBSession => \/.fromTryCatchThrowable[A, SQLException](f(s)) }
      }
    }
  }

}
