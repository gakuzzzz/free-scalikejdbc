package scalikejdbc.free

import org.scalacheck.Gen
import org.scalatest.FunSpec
import scalikejdbc._
import org.scalatest.prop.PropertyChecks._

import scala.util.Try
import scalaz.{\/-, -\/, Free}
import scalaz.Free._

class InterpreterSpec extends FunSpec with Fixtures {

  private lazy val a = Account.syntax("a")
  private lazy val ac = Account.column

  def create[F[_]](name: String)(implicit S: ScalikeJDBC[F]) = {
    import S._
    for {
      id      <- generateKey(insert.into(Account).namedValues(ac.name -> name))
      account <- single(select.from(Account as a).where.eq(a.id, id))(Account(a))
    } yield account
  }

  def findAll[F[_]](implicit S: ScalikeJDBC[F]) = {
    import S._
    for {
      accounts <- seq(select.from(Account as a).orderBy(a.id))(Account(a))
    } yield accounts
  }
  
  def error[F[_]](name: String)(implicit S: ScalikeJDBC[F]) = {
    import S._
    for {
      id      <- generateKey(insert.into(Account).namedValues(ac.name -> name))
      account <- single(sql"invalid SQL".map(Account(a)))
    } yield account
  }

  describe("auto interpreter") {

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val p = create[Query](name)
        val account = Free.runFC(p)(Interpreter.auto)
        assert(account.map(_.name) === Some(name))
      }
    }

    it("should separate transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = Free.runFC(findAll[Query])(Interpreter.auto)
        val errorResult = Try(Free.runFC(error[Query](name))(Interpreter.auto))
        assert(errorResult.isFailure)
        val all2 = Free.runFC(findAll[Query])(Interpreter.auto)
        assert(all1.size + 1 === all2.size)
      }
    }
  }

  describe("safe interpreter") {

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val p = create[Query](name)
        val account = Free.runFC(p)(Interpreter.safe)
        assert(account.isRight)
        assert(account.getOrElse(None).map(_.name) === Some(name))
      }
    }

    it("should separate transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = Free.runFC(findAll[Query])(Interpreter.safe)
        val errorResult = Free.runFC(error[Query](name))(Interpreter.safe)
        assert(errorResult.isLeft)
        val all2 = Free.runFC(findAll[Query])(Interpreter.safe)
        assert(all1.map(_.size + 1) == all2.map(_.size))
      }
    }

  }

  describe("transaction interpreter") {

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val p = create[Query](name)
        val account = DB.localTx(Free.runFC(p)(Interpreter.transaction).run)
        assert(account.map(_.name) === Some(name))
      }
    }

    it("should control transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = DB.localTx(Free.runFC(findAll[Query])(Interpreter.transaction).run)
        val errorResult = Try(DB.localTx(Free.runFC(error[Query](name))(Interpreter.transaction).run))
        assert(errorResult.isFailure)
        val all2 = DB.localTx(Free.runFC(findAll[Query])(Interpreter.transaction).run)
        assert(all1.size === all2.size)
      }
    }

  }

  describe("safeTransaction interpreter") {

    import Interpreter.SQLEither.TxBoundary

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val p = create[Query](name)
        val account = DB.localTx(Free.runFC(p)(Interpreter.safeTransaction).run)
        assert(account.isRight)
        assert(account.getOrElse(None).map(_.name) === Some(name))
      }
    }

    it("should control transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = DB.localTx(Free.runFC(findAll[Query])(Interpreter.safeTransaction).run)
        val errorResult = DB.localTx(Free.runFC(error[Query](name))(Interpreter.safeTransaction).run)
        assert(errorResult.isLeft)
        val all2 = DB.localTx(Free.runFC(findAll[Query])(Interpreter.safeTransaction).run)
        assert(all1.map(_.size) == all2.map(_.size))
      }
    }

  }

  describe("tester interpreter") {

    import Interpreter.TesterBuffer

    it("should not execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = Free.runFC(findAll[Query])(Interpreter.auto)
        val p = create[Query](name)
        val (TesterBuffer(_, queries), account) = Free.runFC(p)(Interpreter.tester).run(TesterBuffer(Vector(3L, Option(Account(3, name)))))
        assert(account.map(_.name) === Some(name))
        assert(queries(0)._1 === s"insert into account (name) values (?)")
        val all2 = Free.runFC(findAll[Query])(Interpreter.auto)
        assert(all1.size === all2.size)
      }
    }

  }


}
