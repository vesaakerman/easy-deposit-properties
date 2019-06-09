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
package nl.knaw.dans.easy.properties

import java.nio.file.Path

import better.files.File
import org.rogach.scallop.{ ScallopConf, ScallopOption, Subcommand }

class CommandLineOptions(args: Array[String], configuration: Configuration) extends ScallopConf(args) {
  appendDefaultToDescription = true
  editBuilder(_.setHelpWidth(110))
  printedName = "easy-deposit-properties"
  private val SUBCOMMAND_SEPARATOR = "---\n"
  val description: String = s"""Service for keeping track of the deposit properties"""
  val synopsis: String =
    s"""
       |  $printedName load-props <properties-file>
       |  $printedName run-service""".stripMargin

  version(s"$printedName v${ configuration.version }")
  banner(
    s"""
       |  $description
       |
       |Usage:
       |
       |$synopsis
       |
       |Options:
       |""".stripMargin)

  val loadProps = new Subcommand("load-props") {
    descr("Load a deposit.properties file and import it in the backend repository.")
    private val props: ScallopOption[Path] = trailArg[Path](
      name = "<properties-file>",
      descr = "The deposit.properties file to be read.",
    )
    validatePathExists(props)
    validatePathIsFile(props)

    val properties = props.map(File(_))
    footer(SUBCOMMAND_SEPARATOR)
  }

  val runService = new Subcommand("run-service") {
    descr(
      "Starts EASY Deposit Properties as a daemon that services HTTP requests")
    footer(SUBCOMMAND_SEPARATOR)
  }

  addSubcommand(loadProps)
  addSubcommand(runService)

  footer("")
}
