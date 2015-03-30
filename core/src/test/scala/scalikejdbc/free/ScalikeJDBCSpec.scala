package scalikejdbc.free

import org.scalatest.FunSpec
import org.scalatest.prop.PropertyChecks._
import org.scalacheck.Gen
import scalikejdbc._

class ScalikeJDBCSpec extends FunSpec with Fixtures {

  private lazy val a = Account.syntax("a")
  private lazy val ac = Account.column

  def create[F[_]](name: String)(implicit S: ScalikeJDBC[F]) = {
    import S._
    for {
      id      <- generateKey(insert.into(Account).namedValues(ac.name -> name))
      _       <- generateKey(sql"INSERT INTO account (name) VALUES ('test')")
      account <- single(select.from(Account as a).where.eq(a.id, id))(Account(a))
      _       <- single(sql"SELECT ${a.result.*} FROM ${Account as a} WHERE ${a.id} = $id".map(Account(a)))
      _       <- vector(select.from(Account as a).orderBy(a.id))(Account(a))
      _       <- vector(sql"SELECT ${a.result.*} FROM ${Account as a}".map(Account(a)))
      _       <- first(select.from(Account as a).where.eq(a.id, id))(Account(a))
      _       <- first(sql"SELECT ${a.result.*} FROM ${Account as a} WHERE ${a.id} = $id".map(Account(a)))
      _       <- foldLeft(select.from(Account as a).orderBy(a.id))("")((acc, rs) => s"$acc:${Account(a)(rs).name}")
      _       <- foldLeft(sql"SELECT ${a.result.*} FROM ${Account as a} ORDER BY ${a.id}")("")((acc, rs) => s"$acc:${Account(a)(rs).name}")
      _       <- S.execute(insert.into(Account).namedValues(ac.name -> "test"))
      _       <- S.execute(sql"INSERT INTO account (name) VALUES ('test')")
      _       <- update(insert.into(Account).namedValues(ac.name -> "test"))
      _       <- update(sql"INSERT INTO account (name) VALUES ('test')")
    } yield account
  }

  describe("Free ScalikeJDBC") {

    it("should build Query") {
      forAll(Gen.alphaStr) { name: String =>
        val account = Interpreter.auto.run(create(name))
        assert(account.map(_.name) === Some(name))
      }
    }

  }


}
