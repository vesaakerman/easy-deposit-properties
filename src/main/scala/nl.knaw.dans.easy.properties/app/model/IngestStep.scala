/**
 * Copyright (C) 2019 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.properties.app.model

import nl.knaw.dans.easy.properties.app.model.IngestStep.StepLabel.StepLabel

case class IngestStep(id: String, step: StepLabel, timestamp: Timestamp)

object IngestStep {

  object StepLabel extends Enumeration {
    type StepLabel = Value

    // @formatter:off
    val VALIDATE     : StepLabel = Value("VALIDATE")
    val PID_GENERATOR: StepLabel = Value("PID_GENERATOR")
    val FEDORA       : StepLabel = Value("FEDORA")
    val SPRINGFIELD  : StepLabel = Value("SPRINGFIELD")
    val BAGSTORE     : StepLabel = Value("BAGSTORE")
    val BAGINDEX     : StepLabel = Value("BAGINDEX") // TODO seems never used in easy-ingest-flow
    val SOLR4FILES   : StepLabel = Value("SOLR4FILES")
    val COMPLETED    : StepLabel = Value("COMPLETED") // TODO new state instead of removing it from the properties
    // @formatter:on
  }
}
