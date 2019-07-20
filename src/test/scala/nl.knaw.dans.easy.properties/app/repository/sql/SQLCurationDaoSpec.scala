package nl.knaw.dans.easy.properties.app.repository.sql

import java.util.UUID

import cats.scalatest.EitherValues
import nl.knaw.dans.easy.properties.app.model.curation.{ Curation, InputCuration }
import nl.knaw.dans.easy.properties.app.repository.{ InvalidValueError, NoSuchDepositError }
import nl.knaw.dans.easy.properties.fixture.{ DatabaseDataFixture, DatabaseFixture, FileSystemSupport, TestSupportFixture }
import org.joda.time.DateTime

class SQLCurationDaoSpec extends TestSupportFixture
  with FileSystemSupport
  with DatabaseFixture
  with DatabaseDataFixture
  with EitherValues {

  "getById" should "find curation configurations identified by their curationId" in {
    val curations = new SQLCurationDao

    curations.getById(Seq("2", "4", "9")).value should contain only(
      "2" -> Some(curation2),
      "4" -> Some(curation4),
      "9" -> Some(curation9),
    )
  }

  it should "return a None if the curationId is unknown" in {
    val curations = new SQLCurationDao
    val unknowncurationId = "102"

    curations.getById(Seq(unknowncurationId)).value should contain only (unknowncurationId -> Option.empty)
  }

  it should "return an empty collection when the input collection is empty" in {
    val curations = new SQLCurationDao

    curations.getById(Seq.empty).value shouldBe empty
  }

  it should "fail when an invalid curationId is given" in {
    val curations = new SQLCurationDao

    curations.getById(Seq("2", "invalid-id", "29")).leftValue shouldBe InvalidValueError("invalid id 'invalid-id'")
  }

  "getCurrent" should "return the current curation configurations of the given deposits" in {
    val curations = new SQLCurationDao

    curations.getCurrent(Seq(depositId1, depositId2)).value should contain only(
      depositId1 -> Some(curation2),
      depositId2 -> None,
    )
  }

  it should "return a None if the depositId is unknown" in {
    val curations = new SQLCurationDao
    val depositId6 = UUID.fromString("00000000-0000-0000-0000-000000000006")

    curations.getCurrent(Seq(depositId6)).value should contain only (depositId6 -> Option.empty)
  }

  it should "return an empty collection when the input collection is empty" in {
    val curations = new SQLCurationDao

    curations.getCurrent(Seq.empty).value shouldBe empty
  }

  "getAll" should "return all curation configurations associated with the given deposits" in {
    val curations = new SQLCurationDao

    curations.getAll(Seq(depositId4, depositId2)).value should contain only(
      depositId4 -> Seq(curation6, curation7),
      depositId2 -> Seq.empty,
    )
  }

  it should "return a None if the depositId is unknown" in {
    val curations = new SQLCurationDao
    val depositId6 = UUID.fromString("00000000-0000-0000-0000-000000000006")

    curations.getAll(Seq(depositId6)).value should contain only (depositId6 -> Seq.empty)
  }

  it should "return an empty collection when the input collection is empty" in {
    val curations = new SQLCurationDao

    curations.getAll(Seq.empty).value shouldBe empty
  }

  "store" should "insert a new curation into the database" in {
    val curations = new SQLCurationDao
    val timestamp = new DateTime(2019, 7, 20, 21, 12, timeZone)
    val inputCuration = InputCuration(isNewVersion = true, isRequired = true, isPerformed = false, "my-username", "foo@bar.com", timestamp)
    val expectedCuration = Curation("10", isNewVersion = true, isRequired = true, isPerformed = false, "my-username", "foo@bar.com", timestamp)

    curations.store(depositId1, inputCuration).value shouldBe expectedCuration
    curations.getById(Seq("10")).value should contain only ("10" -> Some(expectedCuration))
    curations.getCurrent(Seq(depositId1)).value should contain only (depositId1 -> Some(expectedCuration))
    curations.getAll(Seq(depositId1)).value.toMap.apply(depositId1) should contain(expectedCuration)
  }

  it should "fail when the given depositId does not exist" in {
    val curations = new SQLCurationDao
    val depositId6 = UUID.fromString("00000000-0000-0000-0000-000000000006")
    val timestamp = new DateTime(2019, 7, 18, 22, 38, timeZone)
    val inputCuration = InputCuration(isNewVersion = true, isRequired = true, isPerformed = false, "my-username", "foo@bar.com", timestamp)

    curations.store(depositId6, inputCuration).leftValue shouldBe NoSuchDepositError(depositId6)
  }

  "getDepositsById" should "find deposits identified by these curationIds" in {
    val curations = new SQLCurationDao

    curations.getDepositsById(Seq("1", "4", "7", "10")).value should contain only(
      "1" -> Some(deposit1),
      "4" -> Some(deposit3),
      "7" -> Some(deposit4),
      "10" -> None,
    )
  }

  it should "return a None if the curationId is unknown" in {
    val curations = new SQLCurationDao
    val unknowncurationId = "102"

    curations.getDepositsById(Seq(unknowncurationId)).value should contain only (unknowncurationId -> Option.empty)
  }

  it should "return an empty collection when the input collection is empty" in {
    val curations = new SQLCurationDao

    curations.getDepositsById(Seq.empty).value shouldBe empty
  }

  it should "fail when an invalid curationId is given" in {
    val curations = new SQLCurationDao

    curations.getDepositsById(Seq("12", "invalid-id", "29")).leftValue shouldBe InvalidValueError("invalid id 'invalid-id'")
  }
}
