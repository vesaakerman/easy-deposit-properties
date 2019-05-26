package nl.knaw.dans.easy.properties.app.model.ingestStep

object IngestStepLabel extends Enumeration {
  type IngestStepLabel = Value

  // @formatter:off
  val VALIDATE     : IngestStepLabel = Value("VALIDATE")
  val PID_GENERATOR: IngestStepLabel = Value("PID_GENERATOR")
  val FEDORA       : IngestStepLabel = Value("FEDORA")
  val SPRINGFIELD  : IngestStepLabel = Value("SPRINGFIELD")
  val BAGSTORE     : IngestStepLabel = Value("BAGSTORE")
  val BAGINDEX     : IngestStepLabel = Value("BAGINDEX") // TODO seems never used in easy-ingest-flow
  val SOLR4FILES   : IngestStepLabel = Value("SOLR4FILES")
  val COMPLETED    : IngestStepLabel = Value("COMPLETED") // TODO new state instead of removing it from the properties
  // @formatter:on
}
