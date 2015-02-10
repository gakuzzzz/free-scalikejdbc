package scalikejdbc.free

import scalikejdbc._
import scalikejdbc.config._

import scalaz._

object TestMain extends App {

  private val a = Account.syntax("a")
  private val ac = Account.column

  def program[F[_]](implicit S: ScalikeJDBC[F]) = {
    import S._
    for {
      _  <- execute(sql"CREATE TABLE account (id SERIAL PRIMARY KEY, name TEXT NOT NULL)")
      i1 <- generateKey(sql"INSERT INTO account (name) VALUES ('Alice')")
      i2 <- generateKey(insert.into(Account).namedValues(ac.name -> "Bob"))
      l1 <- seq(sql"SELECT ${a.result.*} FROM ${Account as a}".map(Account(a)))
      l2 <- seq(select.from(Account as a))(Account(a))
      o1 <- first(sql"SELECT * FROM account ORDER BY id".map(_.int("id")))
      o2 <- first(select.from(Account as a).orderBy(a.id))(_.int(a.resultName.id))
      n  <- foldLeft(sql"SELECT name FROM account")("") { (s, rs) => s ++ rs.get[String](1) }
    } yield (i1, l1, o1, n)
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
