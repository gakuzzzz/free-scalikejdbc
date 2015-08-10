package entity

import scalikejdbc._
import scalikejdbc.binders._

import scalaz.Tag._
import scalaz._

case class Account(id: Int @@ Account, name: String) {

}

object Account extends SQLSyntaxSupport[Account] {

  def apply(s: SyntaxProvider[Account])(rs: WrappedResultSet): Account = autoConstruct(rs, s)

  val tagOf: TagOf[Account] = Tag.of[Account]

}
