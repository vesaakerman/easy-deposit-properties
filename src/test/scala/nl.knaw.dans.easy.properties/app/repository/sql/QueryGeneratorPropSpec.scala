package nl.knaw.dans.easy.properties.app.repository.sql

import cats.data.NonEmptyList
import nl.knaw.dans.easy.properties.app.model.SeriesFilter.SeriesFilter
import nl.knaw.dans.easy.properties.app.model.contentType.{ ContentTypeValue, DepositContentTypeFilter }
import nl.knaw.dans.easy.properties.app.model.curator.DepositCuratorFilter
import nl.knaw.dans.easy.properties.app.model.ingestStep.{ DepositIngestStepFilter, IngestStepLabel }
import nl.knaw.dans.easy.properties.app.model.state.{ DepositStateFilter, StateLabel }
import nl.knaw.dans.easy.properties.app.model.{ DepositCurationPerformedFilter, DepositCurationRequiredFilter, DepositDoiActionFilter, DepositDoiRegisteredFilter, DepositId, DepositIsNewVersionFilter, DoiAction, SeriesFilter }
import nl.knaw.dans.easy.properties.app.repository.DepositFilters
import org.scalacheck.Arbitrary._
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{ Matchers, PropSpec }

class QueryGeneratorPropSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {

  def genFromEnum[E <: Enumeration](enum: E): Gen[E#Value] = {
    Gen.oneOf(enum.values.toList)
  }

  def genSeriesFilter: Gen[SeriesFilter] = genFromEnum(SeriesFilter)

  def genDepositFilter[E <: Enumeration, T](valueEnum: E)(f: (E#Value, SeriesFilter) => T): Gen[T] = {
    for {
      value <- genFromEnum(valueEnum)
      filter <- genSeriesFilter
    } yield f(value, filter)
  }

  def genDepositFilter[T: Arbitrary, S](f: (T, SeriesFilter) => S): Gen[S] = {
    for {
      value <- implicitly[Arbitrary[T]].arbitrary
      filter <- genSeriesFilter
    } yield f(value, filter)
  }

  def genDepositFilters(): Gen[DepositFilters] = {
    for {
      depositorId <- arbitrary[Option[String]]
      bagName <- arbitrary[Option[String]]
      stateFilter <- Gen.option(genDepositFilter(StateLabel)(DepositStateFilter))
      ingestStepFilter <- Gen.option(genDepositFilter(IngestStepLabel)(DepositIngestStepFilter))
      doiRegisteredFilter <- Gen.option(genDepositFilter(DepositDoiRegisteredFilter))
      doiActionFilter <- Gen.option(genDepositFilter(DoiAction)(DepositDoiActionFilter))
      curationFilter <- Gen.option(genDepositFilter(DepositCuratorFilter))
      isNewVersionFilter <- Gen.option(genDepositFilter(DepositIsNewVersionFilter))
      curationRequiredFilter <- Gen.option(genDepositFilter(DepositCurationRequiredFilter))
      curationPerformedFilter <- Gen.option(genDepositFilter(DepositCurationPerformedFilter))
      contentTypeFilter <- Gen.option(genDepositFilter(ContentTypeValue)(DepositContentTypeFilter))
    } yield DepositFilters(
      depositorId,
      bagName,
      stateFilter,
      ingestStepFilter,
      doiRegisteredFilter,
      doiActionFilter,
      curationFilter,
      isNewVersionFilter,
      curationRequiredFilter,
      curationPerformedFilter,
      contentTypeFilter,
    )
  }

  property("The number of generated question marks in the query should equal the number of elements in the value list in QueryGenerator.searchDeposits") {
    forAll(genDepositFilters())(filters => {
      val (query, values) = QueryGenerator.searchDeposits(filters)
      query.count(_ == '?') shouldBe values.size
    })
  }

  property("The number of generated question marks in the query should equal the number of elements in the value list in QueryGenerator.getLastModifiedDate") {
    forAll(arbitrary[List[DepositId]])(depositIds => {
      whenever(depositIds.nonEmpty) {
        val (query, values) = QueryGenerator.getLastModifiedDate(NonEmptyList.fromListUnsafe(depositIds))
        query.count(_ == '?') shouldBe values.size
      }
    })
  }
}
