package nl.knaw.dans.easy.properties.app.repository.sql

import java.util.UUID

import cats.scalatest.EitherValues
import nl.knaw.dans.easy.properties.app.model.springfield.{ InputSpringfield, Springfield, SpringfieldPlayMode }
import nl.knaw.dans.easy.properties.app.repository.{ InvalidValueError, NoSuchDepositError }
import nl.knaw.dans.easy.properties.fixture.{ DatabaseDataFixture, DatabaseFixture, FileSystemSupport, TestSupportFixture }
import org.joda.time.DateTime

class SQLSpringfieldDaoSpec extends TestSupportFixture
  with FileSystemSupport
  with DatabaseFixture
  with DatabaseDataFixture
  with EitherValues {

  "getById" should "find springfield configurations identified by their springfieldId" in {
    val springfields = new SQLSpringfieldDao

    springfields.getById(Seq("0", "2", "1")).value should contain only(
      "0" -> Some(springfield0),
      "2" -> Some(springfield2),
      "1" -> Some(springfield1),
    )
  }

  it should "return a None if the springfieldId is unknown" in {
    val springfields = new SQLSpringfieldDao
    val unknownspringfieldId = "102"

    springfields.getById(Seq(unknownspringfieldId)).value should contain only (unknownspringfieldId -> Option.empty)
  }

  it should "return an empty collection when the input collection is empty" in {
    val springfields = new SQLSpringfieldDao

    springfields.getById(Seq.empty).value shouldBe empty
  }

  it should "fail when an invalid springfieldId is given" in {
    val springfields = new SQLSpringfieldDao

    springfields.getById(Seq("12", "invalid-id", "29")).leftValue shouldBe InvalidValueError("invalid id 'invalid-id'")
  }

  "getCurrent" should "return the current springfield configurations of the given deposits" in {
    val springfields = new SQLSpringfieldDao

    springfields.getCurrent(Seq(depositId1, depositId5)).value should contain only(
      depositId1 -> Some(springfield0),
      depositId5 -> None,
    )
  }

  it should "return a None if the depositId is unknown" in {
    val springfields = new SQLSpringfieldDao
    val depositId6 = UUID.fromString("00000000-0000-0000-0000-000000000006")

    springfields.getCurrent(Seq(depositId6)).value should contain only (depositId6 -> Option.empty)
  }

  it should "return an empty collection when the input collection is empty" in {
    val springfields = new SQLSpringfieldDao

    springfields.getCurrent(Seq.empty).value shouldBe empty
  }

  "getAll" should "return all springfield configurations associated with the given deposits" in {
    val springfields = new SQLSpringfieldDao

    springfields.getAll(Seq(depositId2, depositId5)).value should contain only(
      depositId2 -> Seq(springfield1, springfield2),
      depositId5 -> Seq.empty,
    )
  }

  it should "return a None if the depositId is unknown" in {
    val springfields = new SQLSpringfieldDao
    val depositId6 = UUID.fromString("00000000-0000-0000-0000-000000000006")

    springfields.getAll(Seq(depositId6)).value should contain only (depositId6 -> Seq.empty)
  }

  it should "return an empty collection when the input collection is empty" in {
    val springfields = new SQLSpringfieldDao

    springfields.getAll(Seq.empty).value shouldBe empty
  }

  "store" should "insert a new state into the database" in {
    val springfields = new SQLSpringfieldDao
    val timestamp = new DateTime(2019, 7, 19, 22, 45, timeZone)
    val inputSpringfield = InputSpringfield("ddd", "uuu", "ccc", SpringfieldPlayMode.MENU, timestamp)
    val expectedState = Springfield("3", "ddd", "uuu", "ccc", SpringfieldPlayMode.MENU, timestamp)

    springfields.store(depositId1, inputSpringfield).value shouldBe expectedState
    springfields.getById(Seq("3")).value should contain only ("3" -> Some(expectedState))
    springfields.getCurrent(Seq(depositId1)).value should contain only (depositId1 -> Some(expectedState))
    springfields.getAll(Seq(depositId1)).value.toMap.apply(depositId1) should contain(expectedState)
  }

  it should "fail when the given depositId does not exist" in {
    val springfields = new SQLSpringfieldDao
    val depositId6 = UUID.fromString("00000000-0000-0000-0000-000000000006")
    val timestamp = new DateTime(2019, 7, 18, 22, 38, timeZone)
    val inputSpringfield = InputSpringfield("domain", "user", "collection", SpringfieldPlayMode.MENU, timestamp)

    springfields.store(depositId6, inputSpringfield).leftValue shouldBe NoSuchDepositError(depositId6)
  }

  "getDepositsById" should "find deposits identified by these springfieldIds" in {
    val springfields = new SQLSpringfieldDao

    springfields.getDepositsById(Seq("0", "1", "2")).value should contain only(
      "0" -> Some(deposit1),
      "1" -> Some(deposit2),
      "2" -> Some(deposit2),
    )
  }

  it should "return a None if the springfieldId is unknown" in {
    val springfields = new SQLSpringfieldDao
    val unknownspringfieldId = "102"

    springfields.getDepositsById(Seq(unknownspringfieldId)).value should contain only (unknownspringfieldId -> Option.empty)
  }

  it should "return an empty collection when the input collection is empty" in {
    val springfields = new SQLSpringfieldDao

    springfields.getDepositsById(Seq.empty).value shouldBe empty
  }

  it should "fail when an invalid springfieldId is given" in {
    val springfields = new SQLSpringfieldDao

    springfields.getDepositsById(Seq("12", "invalid-id", "29")).leftValue shouldBe InvalidValueError("invalid id 'invalid-id'")
  }
}
