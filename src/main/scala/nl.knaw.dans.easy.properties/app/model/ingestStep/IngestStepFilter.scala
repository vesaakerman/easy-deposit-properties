package nl.knaw.dans.easy.properties.app.model.ingestStep

object IngestStepFilter extends Enumeration {
  type IngestStepFilter = Value

  // @formatter:off
  val LATEST: IngestStepFilter = Value("LATEST")
  val ALL   : IngestStepFilter = Value("ALL")
  // @formatter:on
}
