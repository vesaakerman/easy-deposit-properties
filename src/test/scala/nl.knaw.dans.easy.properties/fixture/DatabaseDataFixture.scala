package nl.knaw.dans.easy.properties.fixture

import java.util.UUID

import nl.knaw.dans.easy.properties.app.model.springfield.{ Springfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.model.state.{ State, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ Deposit, DepositId }
import org.joda.time.{ DateTime, DateTimeZone }
import org.scalatest.{ BeforeAndAfterEach, Suite }
import resource.managed

trait DatabaseDataFixture extends BeforeAndAfterEach {
  this: DatabaseFixture with Suite =>

  val timeZone: DateTimeZone = DateTimeZone.UTC

  val depositId1: DepositId = UUID.fromString("00000000-0000-0000-0000-000000000001")
  val depositId2: DepositId = UUID.fromString("00000000-0000-0000-0000-000000000002")
  val depositId3: DepositId = UUID.fromString("00000000-0000-0000-0000-000000000003")
  val depositId4: DepositId = UUID.fromString("00000000-0000-0000-0000-000000000004")
  val depositId5: DepositId = UUID.fromString("00000000-0000-0000-0000-000000000005")

  val deposit1 = Deposit(depositId1, Option("bag1"), new DateTime(2019, 1, 1, 0, 0, timeZone), "user001")
  val deposit2 = Deposit(depositId2, Option.empty, new DateTime(2019, 2, 2, 0, 0, timeZone), "user001")
  val deposit3 = Deposit(depositId3, Option("bag3"), new DateTime(2019, 3, 3, 0, 0, timeZone), "user002")
  val deposit4 = Deposit(depositId4, Option("bag4"), new DateTime(2019, 4, 4, 0, 0, timeZone), "user001")
  val deposit5 = Deposit(depositId5, Option("bag5"), new DateTime(2019, 5, 5, 0, 0, timeZone), "user002")

  def fillDepositTable(): Unit = {
    prepareTest {
      s"""|INSERT INTO Deposit
          |VALUES ('$depositId1', 'bag1', '2019-01-01 00:00:00.000000+0:00', 'user001'),
          |       ('$depositId2',  null , '2019-02-02 00:00:00.000000+0:00', 'user001'),
          |       ('$depositId3', 'bag3', '2019-03-03 00:00:00.000000+0:00', 'user002'),
          |       ('$depositId4', 'bag4', '2019-04-04 00:00:00.000000+0:00', 'user001'),
          |       ('$depositId5', 'bag5', '2019-05-05 00:00:00.000000+0:00', 'user002');""".stripMargin
    }
  }

  val state10 = State("0", StateLabel.DRAFT, "draft with continued deposit", new DateTime(2019, 1, 1, 0, 0, timeZone))
  val state11 = State("1", StateLabel.DRAFT, "draft with continued deposit", new DateTime(2019, 1, 1, 1, 1, timeZone))
  val state12 = State("2", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 1, 1, 2, 2, timeZone))
  val state13 = State("3", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 1, 1, 3, 3, timeZone))
  val state14 = State("4", StateLabel.SUBMITTED, "deposit is processing", new DateTime(2019, 1, 1, 4, 4, timeZone))
  val state15 = State("5", StateLabel.ARCHIVED, "deposit is archived", new DateTime(2019, 1, 1, 5, 5, timeZone))

  val state20 = State("6", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 2, 2, 0, 0, timeZone))
  val state21 = State("7", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 2, 2, 1, 1, timeZone))
  val state22 = State("8", StateLabel.SUBMITTED, "deposit is processing", new DateTime(2019, 2, 2, 2, 2, timeZone))
  val state23 = State("9", StateLabel.ARCHIVED, "deposit is archived", new DateTime(2019, 2, 2, 3, 3, timeZone))

  val state30 = State("10", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 3, 3, 0, 0, timeZone))
  val state31 = State("11", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 3, 3, 1, 1, timeZone))
  val state32 = State("12", StateLabel.INVALID, "deposit is invalid", new DateTime(2019, 3, 3, 2, 2, timeZone))

  val state40 = State("13", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 4, 4, 0, 0, timeZone))
  val state41 = State("14", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 4, 4, 1, 1, timeZone))
  val state42 = State("15", StateLabel.ARCHIVED, "deposit is archived", new DateTime(2019, 4, 4, 2, 2, timeZone))

  val state50 = State("16", StateLabel.UPLOADED, "deposit upload has been completed", new DateTime(2019, 5, 5, 0, 0, timeZone))
  val state51 = State("17", StateLabel.FINALIZING, "deposit is finalizing", new DateTime(2019, 5, 5, 1, 1, timeZone))
  val state52 = State("18", StateLabel.SUBMITTED, "deposit is processing", new DateTime(2019, 5, 5, 2, 2, timeZone))
  val state53 = State("19", StateLabel.REJECTED, "deposit is rejected", new DateTime(2019, 5, 5, 3, 3, timeZone))

  def fillStateTable(): Unit = {
    prepareTest {
      """INSERT INTO State (depositId, label, description, timestamp)
        |VALUES ('00000000-0000-0000-0000-000000000001', 'DRAFT'     , 'draft with continued deposit'     , '2019-01-01 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'DRAFT'     , 'draft with continued deposit'     , '2019-01-01 01:01:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'UPLOADED'  , 'deposit upload has been completed', '2019-01-01 02:02:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'FINALIZING', 'deposit is finalizing'            , '2019-01-01 03:03:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'SUBMITTED' , 'deposit is processing'            , '2019-01-01 04:04:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'ARCHIVED'  , 'deposit is archived'              , '2019-01-01 05:05:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'UPLOADED'  , 'deposit upload has been completed', '2019-02-02 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'FINALIZING', 'deposit is finalizing'            , '2019-02-02 01:01:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'SUBMITTED' , 'deposit is processing'            , '2019-02-02 02:02:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'ARCHIVED'  , 'deposit is archived'              , '2019-02-02 03:03:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000003', 'UPLOADED'  , 'deposit upload has been completed', '2019-03-03 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000003', 'FINALIZING', 'deposit is finalizing'            , '2019-03-03 01:01:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000003', 'INVALID'   , 'deposit is invalid'               , '2019-03-03 02:02:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'UPLOADED'  , 'deposit upload has been completed', '2019-04-04 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'FINALIZING', 'deposit is finalizing'            , '2019-04-04 01:01:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'ARCHIVED'  , 'deposit is archived'              , '2019-04-04 02:02:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'UPLOADED'  , 'deposit upload has been completed', '2019-05-05 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'FINALIZING', 'deposit is finalizing'            , '2019-05-05 01:01:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'SUBMITTED' , 'deposit is processing'            , '2019-05-05 02:02:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'REJECTED'  , 'deposit is rejected'              , '2019-05-05 03:03:00.000000+00:00');""".stripMargin
    }
  }

  def fillIdentifierTable(): Unit = {
    prepareTest {
      """INSERT INTO Identifier (depositId, identifierSchema, identifierValue, timestamp)
        |VALUES ('00000000-0000-0000-0000-000000000001', 'bag-store', '00000000-0000-0000-0000-000000000001', '2019-01-01 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'doi'      , '10.5072/dans-a1b-cde2'               , '2019-01-01 00:01:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'urn'      , 'urn:nbn:123456'                      , '2019-01-01 00:02:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'fedora'   , 'easy-dataset:1'                      , '2019-01-01 00:03:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'bag-store', '00000000-0000-0000-0000-000000000002', '2019-02-02 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'doi'      , '10.5072/dans-f3g-hij4'               , '2019-02-02 00:01:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'urn'      , 'urn:nbn:789012'                      , '2019-02-02 00:02:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'fedora'   , 'easy-dataset:2'                      , '2019-02-02 00:03:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000003', 'bag-store', '00000000-0000-0000-0000-000000000003', '2019-03-03 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'bag-store', '00000000-0000-0000-0000-000000000004', '2019-04-04 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'doi'      , '10.5072/dans-p7q-rst8'               , '2019-04-04 00:01:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'urn'      , 'urn:nbn:901234'                      , '2019-04-04 00:02:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'fedora'   , 'easy-dataset:4'                      , '2019-04-04 00:03:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'bag-store', '00000000-0000-0000-0000-000000000005', '2019-05-05 00:00:00.000000+00:00');""".stripMargin
    }
  }

  def fillCurationTable(): Unit = {
    prepareTest {
      """INSERT INTO Curation (depositId, isNewVersion, isRequired, isPerformed, datamanagerUserId, datamanagerEmail, timestamp)
        |VALUES ('00000000-0000-0000-0000-000000000001', 'false', 'true', 'false', 'archie002', 'does.not.exists2@dans.knaw.nl', '2019-01-01 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'false', 'true', 'false', 'archie001', 'does.not.exists1@dans.knaw.nl', '2019-01-01 03:03:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'false', 'true', 'true' , 'archie001', 'does.not.exists1@dans.knaw.nl', '2019-01-01 04:04:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000003', 'true' , 'true', 'false', 'archie001', 'does.not.exists1@dans.knaw.nl', '2019-03-03 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000003', 'true' , 'true', 'false', 'archie002', 'does.not.exists2@dans.knaw.nl', '2019-03-03 04:04:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000003', 'true' , 'true', 'true' , 'archie002', 'does.not.exists2@dans.knaw.nl', '2019-03-03 06:06:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'false', 'true', 'false', 'archie001', 'does.not.exists1@dans.knaw.nl', '2019-04-04 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'false', 'true', 'true' , 'archie001', 'does.not.exists1@dans.knaw.nl', '2019-04-04 04:04:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'false', 'true', 'false', 'archie001', 'does.not.exists1@dans.knaw.nl', '2019-05-05 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'false', 'true', 'true' , 'archie001', 'does.not.exists1@dans.knaw.nl', '2019-05-05 04:04:00.000000+00:00');""".stripMargin
    }
  }

  val springfield0 = Springfield("0", "domain1", "user1", "collection1", SpringfieldPlayMode.CONTINUOUS, new DateTime(2019, 1, 1, 0, 0, timeZone))
  val springfield1 = Springfield("1", "domain1", "user1", "collection1", SpringfieldPlayMode.CONTINUOUS, new DateTime(2019, 2, 2, 0, 0, timeZone))
  val springfield2 = Springfield("2", "domain2", "user2", "collection2", SpringfieldPlayMode.MENU, new DateTime(2019, 2, 2, 2, 2, timeZone))

  def fillSpringfieldTable(): Unit = {
    prepareTest {
      """INSERT INTO Springfield (depositId, domain, springfield_user, collection, playmode, timestamp)
        |VALUES ('00000000-0000-0000-0000-000000000001', 'domain1', 'user1', 'collection1', 'continuous', '2019-01-01 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'domain1', 'user1', 'collection1', 'continuous', '2019-02-02 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'domain2', 'user2', 'collection2', 'menu'      , '2019-02-02 02:02:00.000000+00:00');""".stripMargin
    }
  }

  def fillSimplePropertiesTable(): Unit = {
    prepareTest {
      """INSERT INTO SimpleProperties (depositId, key, value, timestamp)
        |VALUES ('00000000-0000-0000-0000-000000000001', 'ingest-step', 'VALIDATE'     , '2019-01-01 04:05:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'ingest-step', 'PID_GENERATOR', '2019-01-01 04:06:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'ingest-step', 'FEDORA'       , '2019-01-01 04:07:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'ingest-step', 'SPRINGFIELD'  , '2019-01-01 04:08:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'ingest-step', 'BAGSTORE'     , '2019-01-01 04:09:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'ingest-step', 'SOLR4FILES'   , '2019-01-01 04:10:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'ingest-step', 'COMPLETED'    , '2019-01-01 04:11:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'ingest-step', 'VALIDATE'     , '2019-02-02 02:05:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'ingest-step', 'PID_GENERATOR', '2019-02-02 02:06:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'ingest-step', 'FEDORA'       , '2019-02-02 02:07:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'ingest-step', 'SPRINGFIELD'  , '2019-02-02 02:08:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'ingest-step', 'BAGSTORE'     , '2019-02-02 02:09:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'ingest-step', 'SOLR4FILES'   , '2019-02-02 02:10:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'ingest-step', 'COMPLETED'    , '2019-02-02 02:11:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'ingest-step', 'VALIDATE'     , '2019-05-05 04:05:00.000000+00:00'),
        |
        |       ('00000000-0000-0000-0000-000000000001', 'doi-registered', 'false', '2019-01-01 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'doi-registered', 'true' , '2019-01-01 04:07:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'doi-registered', 'false', '2019-02-02 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'doi-registered', 'true' , '2019-02-02 02:07:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'doi-registered', 'false', '2019-05-05 00:00:00.000000+00:00'),
        |
        |       ('00000000-0000-0000-0000-000000000001', 'doi-action', 'update', '2019-01-01 01:01:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'doi-action', 'none'  , '2019-01-01 04:05:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'doi-action', 'create', '2019-02-02 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000003', 'doi-action', 'create', '2019-03-03 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'doi-action', 'create', '2019-04-04 00:00:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'doi-action', 'update', '2019-05-05 00:00:00.000000+00:00'),
        |
        |       ('00000000-0000-0000-0000-000000000001', 'content-type', 'application/zip'         , '2019-01-01 00:05:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000001', 'content-type', 'application/octet-stream', '2019-01-01 00:10:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000002', 'content-type', 'application/zip'         , '2019-02-02 00:05:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000003', 'content-type', 'application/zip'         , '2019-03-03 00:05:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000004', 'content-type', 'application/zip'         , '2019-04-04 00:05:00.000000+00:00'),
        |       ('00000000-0000-0000-0000-000000000005', 'content-type', 'application/zip'         , '2019-05-05 00:05:00.000000+00:00');""".stripMargin
    }
  }

  def prepareTest(query: String): Unit = {
    managed(connection.createStatement())
      .acquireAndGet(_.executeUpdate(query))
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    fillDepositTable()
    fillStateTable()
    fillIdentifierTable()
    fillCurationTable()
    fillSpringfieldTable()
    fillSimplePropertiesTable()
  }
}
