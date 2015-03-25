package scalikejdbc.free

import org.scalacheck.Gen
import org.scalatest.FunSpec
import scalikejdbc._
import org.scalatest.prop.PropertyChecks._

import scala.util.Try
import scalaz.Free

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
        val account = Interpreter.auto.run(create(name))
        assert(account.map(_.name) === Some(name))
      }
    }

    it("should separate transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = Interpreter.auto.run(findAll)
        val errorResult = Try(Interpreter.auto.run(error(name)))
        assert(errorResult.isFailure)
        val all2 = Interpreter.auto.run(findAll)
        assert(all1.size + 1 === all2.size)
      }
    }
  }

  describe("safe interpreter") {

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val account = Interpreter.safe.run(create(name))
        assert(account.isRight)
        assert(account.getOrElse(None).map(_.name) === Some(name))
      }
    }

    it("should separate transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = Interpreter.safe.run(findAll)
        val errorResult = Interpreter.safe.run(error(name))
        assert(errorResult.isLeft)
        val all2 = Interpreter.safe.run(findAll)
        assert(all1.map(_.size + 1) == all2.map(_.size))
      }
    }

  }

  describe("transaction interpreter") {

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val account = DB.localTx(Interpreter.transaction.run(create(name)))
        assert(account.map(_.name) === Some(name))
      }
    }

    it("should control transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = DB.localTx(Interpreter.transaction.run(findAll))
        val errorResult = Try(DB.localTx(Interpreter.transaction.run(error(name))))
        assert(errorResult.isFailure)
        val all2 = DB.localTx(Interpreter.transaction.run(findAll))
        assert(all1.size === all2.size)
      }
    }

  }

  describe("safeTransaction interpreter") {

    import Interpreter.SQLEither.TxBoundary

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val account = DB.localTx(Interpreter.safeTransaction.run(create(name)))
        assert(account.isRight)
        assert(account.getOrElse(None).map(_.name) === Some(name))
      }
    }

    it("should control transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = DB.localTx(Interpreter.safeTransaction.run(findAll))
        val errorResult = DB.localTx(Interpreter.safeTransaction.run(error(name)))
        assert(errorResult.isLeft)
        val all2 = DB.localTx(Interpreter.safeTransaction.run(findAll))
        assert(all1.map(_.size) == all2.map(_.size))
      }
    }

  }

  describe("tester interpreter") {

    import Interpreter.TesterBuffer

    it("should not execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = Interpreter.auto.run(findAll)
        val (TesterBuffer(_, queries), account) = Interpreter.tester.run(create(name)).run(TesterBuffer(Vector(3L, Option(Account(3, name)))))
        assert(account.map(_.name) === Some(name))
        assert(queries(0)._1 === s"insert into account (name) values (?)")
        val all2 = Interpreter.auto.run(findAll)
        assert(all1.size === all2.size)
      }
    }

  }


}
