package nl.knaw.dans.easy.properties.fixture

import java.sql.Connection

import better.files.File
import nl.knaw.dans.easy.properties.app.database.{ DatabaseAccess, DatabaseConfiguration }
import org.scalatest.BeforeAndAfterEach
import resource.{ constant, managed }

import scala.util.Success

trait DatabaseFixture extends BeforeAndAfterEach {
  this: TestSupportFixture with FileSystemSupport =>

  private val databaseDir: File = testDir / "database"

  implicit var connection: Connection = _

  private val dbConfig = DatabaseConfiguration(
    dbDriverClassName = "org.hsqldb.jdbcDriver",
    dbUrl = s"jdbc:hsqldb:file:${ databaseDir.toString }/db"
  )
  private val databaseAccess = new DatabaseAccess(dbConfig) {
    override protected def createConnectionPool: ConnectionPool = {
      val pool = super.createConnectionPool

      managed(pool.getConnection)
        .flatMap(connection => managed(connection.createStatement))
        .and(constant(File(getClass.getClassLoader.getResource("database/db-tables.sql").toURI).contentAsString))
        .map { case (statement, query) => statement.executeUpdate(query) }
        .tried
        .recover { case e => println(e.getMessage); fail("could not create database for testing", e) } shouldBe a[Success[_]]

      connection = pool.getConnection

      pool
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    if (databaseDir.exists)
      databaseDir.delete()

    databaseAccess.initConnectionPool()
  }

  override def afterEach(): Unit = {
    managed(connection.createStatement).acquireAndGet(_.execute("SHUTDOWN"))
    connection.close()
    databaseAccess.closeConnectionPool()
    super.afterEach()
  }
}
