package scalikejdbc.free

import scalikejdbc.free.Query._
import scalikejdbc._

import scalaz.Free.FreeC
import scalaz.{Free, Inject}

sealed class ScalikeJDBC[F[_]](implicit I: Inject[Query, F]) {

  private def lift[A](v: Query[A]): FreeC[F, A] = Free.liftFC(I.inj(v))

  def seq[A](sql: SQL[A, HasExtractor]): FreeC[F, Seq[A]] = lift(GetSeq[A](sql.collection))
  def seq[A](sql: SQLBuilder[_])(f: WrappedResultSet => A): FreeC[F, Seq[A]] = seq(withSQL(sql).map(f))

  def first[A](sql: SQL[A, HasExtractor]): FreeC[F, Option[A]] = lift(GetOption[A](sql.first()))
  def first[A](sql: SQLBuilder[_])(f: WrappedResultSet => A): FreeC[F, Option[A]] = first(withSQL(sql).map(f))

  def single[A](sql: SQL[A, HasExtractor]): FreeC[F, Option[A]] = lift(GetOption[A](sql.single()))
  def single[A](sql: SQLBuilder[_])(f: WrappedResultSet => A): FreeC[F, Option[A]] = single(withSQL(sql).map(f))

  def foldLeft[A](sql: SQL[_, NoExtractor])(init: A)(f: (A, WrappedResultSet) => A): FreeC[F, A] = lift(Fold(sql, init, f))
  def foldLeft[A](sql: SQLBuilder[_])(init: A)(f: (A, WrappedResultSet) => A): FreeC[F, A] = foldLeft(withSQL(sql))(init)(f)


  def execute(sql: SQL[_, NoExtractor]): FreeC[F, Boolean] = lift(Execute(sql.execute()))
  def execute(sql: SQLBuilder[UpdateOperation]): FreeC[F, Boolean] = execute(withSQL(sql))

  def update(sql: SQL[_, NoExtractor]): FreeC[F, Int] = lift(Update(sql.update()))
  def update(sql: SQLBuilder[UpdateOperation]): FreeC[F, Int] = update(withSQL(sql))

  def generateKey(sql: SQL[_, NoExtractor]): FreeC[F, Long] = lift(GenerateKey(sql.updateAndReturnGeneratedKey()))
  def generateKey(sql: SQLBuilder[UpdateOperation]): FreeC[F, Long] = generateKey(withSQL(sql))

}

object ScalikeJDBC {
  implicit def instance[F[_]](implicit I: Inject[Query, F]): ScalikeJDBC[F] = new ScalikeJDBC[F]
}