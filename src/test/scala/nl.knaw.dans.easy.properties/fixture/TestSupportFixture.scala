package nl.knaw.dans.easy.properties.fixture

import org.scalatest.{ EitherValues, FlatSpec, Inside, Inspectors, Matchers, OptionValues }

trait TestSupportFixture extends FlatSpec
  with Matchers
  with Inside
  with OptionValues
  with EitherValues
  with Inspectors
