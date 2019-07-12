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
package nl.knaw.dans.easy.properties.app.graphql.middleware

import nl.knaw.dans.easy.properties.app.graphql.DataContext
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import sangria.execution.Middleware

import scala.language.postfixOps

class Middlewares(profilingConfig: Option[ProfilingConfiguration]) extends DebugEnhancedLogging {

  lazy val values: List[Middleware[DataContext]] = List(
    Some(Authentication),
    Some(timer),
    slowLog,
  ).flatten

  private lazy val slowLog = profilingConfig.map(Profiling(logger.underlying))
  private lazy val timer = new Metrics(logger.underlying)
}
