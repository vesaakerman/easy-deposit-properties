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

import nl.knaw.dans.easy.properties.app.model.State.StateLabel.StateLabel

case class State(label: StateLabel, description: String, timestamp: Timestamp)

object State {

  object StateLabel extends Enumeration {
    type StateLabel = Value

    // @formatter:off
    val DRAFT          : StateLabel = Value("DRAFT")
    val UPLOADED       : StateLabel = Value("UPLOADED")
    val FINALIZING     : StateLabel = Value("FINALIZING")
    val INVALID        : StateLabel = Value("INVALID")
    val SUBMITTED      : StateLabel = Value("SUBMITTED")
    val REJECTED       : StateLabel = Value("REJECTED")
    val FAILED         : StateLabel = Value("FAILED")
    val IN_REVIEW      : StateLabel = Value("IN_REVIEW")
    val ARCHIVED       : StateLabel = Value("ARCHIVED")
    val FEDORA_ARCHIVED: StateLabel = Value("FEDORA_ARCHIVED")
    // @formatter:on
  }
}
