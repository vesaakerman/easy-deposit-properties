package nl.knaw.dans.easy.properties.app.repository.sql

import java.sql.Connection

import nl.knaw.dans.easy.properties.app.repository.{ ContentTypeDao, CurationDao, DepositDao, DoiActionDao, DoiRegisteredDao, IdentifierDao, IngestStepDao, Repository, SpringfieldDao, StateDao }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

class SQLRepo(implicit connection: Connection) extends DebugEnhancedLogging {

  logger.info("new SQLRepo")
  private val depositDao: DepositDao = new SQLDepositDao
  private val stateDao: StateDao = new SQLStateDao
  private val ingestStepDao: IngestStepDao = new SQLIngestStepDao
  private val identifierDao: IdentifierDao = new SQLIdentifierDao
  private val doiRegisteredDao: DoiRegisteredDao = new SQLDoiRegisteredDao
  private val doiActionDao: DoiActionDao = new SQLDoiActionDao
  private val curationDao: CurationDao = new SQLCurationDao
  private val springfieldDao: SpringfieldDao = new SQLSpringfieldDao
  private val contentTypeDao: ContentTypeDao = new SQLContentTypeDao

  def repository: Repository = Repository(
    depositDao,
    stateDao,
    ingestStepDao,
    identifierDao,
    doiRegisteredDao,
    doiActionDao,
    curationDao,
    springfieldDao,
    contentTypeDao,
  )
}
