package scalikejdbc.free

import scalikejdbc._

case class Account(id: Int, name: String) {

}

object Account extends SQLSyntaxSupport[Account] {

  def apply(s: SyntaxProvider[Account])(rs: WrappedResultSet): Account = autoConstruct(rs, s)

}
