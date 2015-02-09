package scalikejdbc.free

import scalikejdbc._
import scalikejdbc.config._

import scalaz.Free.FreeC
import scalaz.{Coyoneda, Monad, ~>}

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

  def runFC[S[_], M[_], A](sa: FreeC[S, A])(interp: S ~> M)(implicit M: Monad[M]): M[A] = {
    sa.foldMap(new (({type λ[x] = Coyoneda[S, x]})#λ ~> M) {
      def apply[B](cy: Coyoneda[S, B]): M[B] = M.map(interp(cy.fi))(cy.k)
    })
  }

  def testApp = runFC(program[Query])(Interpreter)

  DBs.setupAll()

  println(DB.localTx(testApp.run))

}
