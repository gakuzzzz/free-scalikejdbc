package scalikejdbc.free

import scalikejdbc._
import scalikejdbc.config._

import scalaz.Free

object TestMain extends App {

  def program[F[_]](implicit S: ScalikeJDBC[F]) = {
    import S._
    for {
      _  <- execute(sql"CREATE TABLE account (id SERIAL PRIMARY KEY, name TEXT NOT NULL)")
      id <- generateKey(sql"INSERT INTO account (name) VALUES ('Alice'), ('Bob')")
      l  <- seq(sql"SELECT id FROM account".map(_.get[Int](1)))
      o  <- first(sql"SELECT id FROM account ORDER BY id".map(_.get[Int](1)))
    } yield (id, l, o)
  }

  def testApp = Free.runFC(program[Query])(Interpreter)

  DBs.setupAll()

  println(DB.localTx(testApp.run))

}
