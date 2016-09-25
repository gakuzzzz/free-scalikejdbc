package scalikejdbc.free

import scalikejdbc.free.Query._
import scalikejdbc._

import scalaz.{Free, Inject}

sealed class ScalikeJDBC[F[_]](implicit I: Inject[Query, F]) {

  private def lift[A](v: Query[A]): Free[F, A] = Free.liftF(I.inj(v))

  def vector[A](sql: SQL[A, HasExtractor]): Free[F, Vector[A]] = lift(GetVector[A](sql.collection))
  def vector[A](sql: SQLBuilder[_])(f: WrappedResultSet => A): Free[F, Vector[A]] = vector(withSQL(sql).map(f))

  def list[A](sql: SQL[A, HasExtractor]): Free[F, List[A]] = lift(GetList[A](sql.list()))
  def list[A](sql: SQLBuilder[_])(f: WrappedResultSet => A): Free[F, List[A]] = list(withSQL(sql).map(f))

  def first[A](sql: SQL[A, HasExtractor]): Free[F, Option[A]] = lift(GetOption[A](sql.first()))
  def first[A](sql: SQLBuilder[_])(f: WrappedResultSet => A): Free[F, Option[A]] = first(withSQL(sql).map(f))

  def single[A](sql: SQL[A, HasExtractor]): Free[F, Option[A]] = lift(GetOption[A](sql.single()))
  def single[A](sql: SQLBuilder[_])(f: WrappedResultSet => A): Free[F, Option[A]] = single(withSQL(sql).map(f))

  def foldLeft[A](sql: SQL[_, NoExtractor])(init: A)(f: (A, WrappedResultSet) => A): Free[F, A] = lift(Fold(sql, init, f))
  def foldLeft[A](sql: SQLBuilder[_])(init: A)(f: (A, WrappedResultSet) => A): Free[F, A] = foldLeft(withSQL(sql))(init)(f)


  def execute(sql: SQL[_, NoExtractor]): Free[F, Boolean] = lift(Execute(sql.execute()))
  def execute(sql: SQLBuilder[UpdateOperation]): Free[F, Boolean] = execute(withSQL(sql))

  def update(sql: SQL[_, NoExtractor]): Free[F, Int] = lift(Update(sql.update()))
  def update(sql: SQLBuilder[UpdateOperation]): Free[F, Int] = update(withSQL(sql))

  def generateKey(sql: SQL[_, NoExtractor]): Free[F, Long] = lift(GenerateKey(sql.updateAndReturnGeneratedKey()))
  def generateKey(sql: SQLBuilder[UpdateOperation]): Free[F, Long] = generateKey(withSQL(sql))

}

object ScalikeJDBC {
  implicit def instance[F[_]](implicit I: Inject[Query, F]): ScalikeJDBC[F] = new ScalikeJDBC[F]
}