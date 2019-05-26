package nl.knaw.dans.easy.properties.app.model.ingestStep

import nl.knaw.dans.easy.properties.app.model.DepositFilter
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStepFilter.IngestStepFilter
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStepLabel.IngestStepLabel

case class DepositIngestStepFilter(label: IngestStepLabel, filter: IngestStepFilter = IngestStepFilter.LATEST) extends DepositFilter
