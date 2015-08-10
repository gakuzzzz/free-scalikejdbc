package scalikejdbc.tagless

import entity.Account
import org.scalacheck.Gen
import org.scalatest.FunSpec
import org.scalatest.prop.PropertyChecks._
import scalikejdbc._

import scala.util.Try
import scalaz._
import scalaz.syntax.monad._
import scalaz.\/._

class TaglessFinalInterpreterSpec extends FunSpec with Fixtures {

  private lazy val a = Account.syntax("a")
  private lazy val ac = Account.column

  def create[F[_]](name: String)(implicit S: ScalikeJDBC[F], F: Monad[F]) = {
    import S._
    for {
      id      <- generateKey(insert.into(Account).namedValues(ac.name -> name))
      account <- single(select.from(Account as a).where.eq(a.id, id))(Account(a))
    } yield account
  }

  def findAll[F[_]](implicit S: ScalikeJDBC[F], F: Monad[F]) = {
    import S._
    for {
      accounts <- vector(select.from(Account as a).orderBy(a.id))(Account(a))
    } yield accounts
  }
  
  def error[F[_]](name: String)(implicit S: ScalikeJDBC[F], F: Monad[F]) = {
    import S._
    for {
      id      <- generateKey(insert.into(Account).namedValues(ac.name -> name))
      account <- single(sql"invalid SQL".map(Account(a)))
    } yield account
  }

  describe("auto interpreter") {
    import Interpreter.auto._

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val account = create(name)
        assert(account.map(_.name) === Some(name))
      }
    }

    it("should separate transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = findAll
        val errorResult = Try(error(name))
        assert(errorResult.isFailure)
        val all2 = findAll
        assert(all1.size + 1 === all2.size)
      }
    }
  }

  describe("safe interpreter") {
    import Interpreter.safe._

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val account = create(name)
        assert(account.isRight)
        assert(account.getOrElse(None).map(_.name) === Some(name))
      }
    }

    it("should separate transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = findAll
        val errorResult = error(name)
        assert(errorResult.isLeft)
        val all2 = findAll
        assert(all1.map(_.size + 1) == all2.map(_.size))
      }
    }

  }

  describe("transaction interpreter") {
    import Interpreter.transaction._

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val account = DB.localTx(create(name).run)
        assert(account.map(_.name) === Some(name))
      }
    }

    it("should control transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = DB.localTx(findAll.run)
        val errorResult = Try(DB.localTx(error(name).run))
        assert(errorResult.isFailure)
        val all2 = DB.localTx(findAll.run)
        assert(all1.size === all2.size)
      }
    }

  }

  describe("safeTransaction interpreter") {
    import Interpreter.safeTransaction._

    it("should execute Query") {
      forAll(Gen.alphaStr) { name: String =>
        val account = DB.localTx(create(name).run)
        assert(account.isRight)
        assert(account.getOrElse(None).map(_.name) === Some(name))
      }
    }

    it("should control transaction") {
      forAll(Gen.alphaStr) { name: String =>
        val all1 = DB.localTx(findAll.run)
        val errorResult = DB.localTx(error(name).run)
        assert(errorResult.isLeft)
        val all2 = DB.localTx(findAll.run)
        assert(all1.map(_.size) == all2.map(_.size))
      }
    }

  }

}
