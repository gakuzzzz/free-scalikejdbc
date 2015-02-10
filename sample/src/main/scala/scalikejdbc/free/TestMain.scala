package scalikejdbc.free

import scalikejdbc._
import scalikejdbc.config._

import scalaz._

object TestMain extends App {

  def program[F[_]](implicit S: ScalikeJDBC[F]) = {
    import S._
    for {
      _  <- execute(sql"CREATE TABLE account (id SERIAL PRIMARY KEY, name TEXT NOT NULL)")
      id <- generateKey(sql"INSERT INTO account (name) VALUES ('Alice'), ('Bob')")
      l  <- seq(sql"SELECT name FROM account".map(_.get[String](1)))
      o  <- first(sql"SELECT id FROM account ORDER BY id".map(_.get[Int](1)))
    } yield (id, l, o)
  }

  def testApp = Free.runFC(program[Query])(Interpreter.base)

  def failPg[F[_]](implicit S: ScalikeJDBC[F]) = {
    import S._
    for {
      l  <- seq(sql"SELECT foooo FROM account".map(_.get[Int](1)))
      o  <- first(sql"SELECT id FROM account ORDER BY id".map(_.get[Int](1)))
    } yield (l, o)
  }

  def failApp = Free.runFC(failPg[Query])(Interpreter.safe)

  implicit def sqlEitherTxBoundary[A]  = new TxBoundary[Interpreter.SQLEither[A]] {
    def finishTx(result: Interpreter.SQLEither[A], tx: Tx) = {
      result match {
        case -\/(_) => tx.commit()
        case \/-(_) => tx.rollback()
      }
      result
    }
  }

  DBs.setupAll()

  println(DB.localTx(testApp.run))

  println(DB.localTx(failApp.run))

}
