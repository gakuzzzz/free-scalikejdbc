package scalikejdbc.tagless

import org.scalatest.{Suite, BeforeAndAfter, BeforeAndAfterEach}
import scalikejdbc._
import scalikejdbc.config.DBs

trait Fixtures extends BeforeAndAfterEach with BeforeAndAfter { this: Suite =>

  before {
    DBs.setupAll()
  }

  override def beforeEach() {
    sql"CREATE TABLE account (id SERIAL PRIMARY KEY, name TEXT NOT NULL)".update().apply()(AutoSession)
    sql"INSERT INTO account (name) VALUES ('Alice')".update().apply()(AutoSession)
    sql"INSERT INTO account (name) VALUES ('Bob')".update().apply()(AutoSession)
    super.beforeEach()
  }

  override def afterEach() {
    sql"DROP TABLE account".update().apply()(AutoSession)
    super.afterEach()
  }

  after {
    DBs.closeAll()
  }

}
