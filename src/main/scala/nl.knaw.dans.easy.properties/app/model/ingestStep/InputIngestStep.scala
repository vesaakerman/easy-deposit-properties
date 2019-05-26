package nl.knaw.dans.easy.properties.app.model.ingestStep

import nl.knaw.dans.easy.properties.app.model.Timestamp
import nl.knaw.dans.easy.properties.app.model.ingestStep.IngestStepLabel.IngestStepLabel

case class InputIngestStep(step: IngestStepLabel, timestamp: Timestamp)
