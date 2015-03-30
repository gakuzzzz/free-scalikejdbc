package scalikejdbc.free

import scalikejdbc._


sealed abstract class Query[A](private[free] val statement: String, private[free] val parameters: Seq[Any]) {

}


object Query {

  case class GetVector[A](sql: SQLToCollection[A, HasExtractor]) extends Query[Vector[A]](sql.statement, sql.parameters)
  case class GetList[A](sql: SQLToList[A, HasExtractor]) extends Query[List[A]](sql.statement, sql.parameters)
  case class GetOption[A](sql: SQLToOption[A, HasExtractor]) extends Query[Option[A]](sql.statement, sql.parameters)
  case class Fold[A](sql: SQL[_, NoExtractor], init: A, f: (A, WrappedResultSet) => A) extends Query[A](sql.statement, sql.parameters)

  case class Update(sql: SQLUpdate) extends Query[Int](sql.statement, sql.parameters)
  case class GenerateKey(sql: SQLUpdateWithGeneratedKey) extends Query[Long](sql.statement, sql.parameters)
  case class Execute(sql: SQLExecution) extends Query[Boolean](sql.statement, sql.parameters)

}