package scalikejdbc.free

import org.scalatest.{FlatSpec, PropSpec}
import scalikejdbc._

class ScalikeJDBCSpec extends FlatSpec {

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
      n1 <- foldLeft(sql"SELECT name FROM account")("") { (s, rs) => s ++ rs.get[String](1) }
      n2 <- foldLeft(select.from(Account as a))("") { (s, rs) => s ++ Account(a)(rs).name }
    } yield ()
  }




}
