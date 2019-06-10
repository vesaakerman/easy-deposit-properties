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

import java.net.URL

import better.files.File
import better.files.File.root
import nl.knaw.dans.easy.DataciteServiceConfiguration
import nl.knaw.dans.easy.properties.app.database.DatabaseConfiguration
import org.apache.commons.configuration.PropertiesConfiguration

case class Configuration(version: String,
                         serverPort: Int,
                         databaseConfig: DatabaseConfiguration,
                         dataciteConfig: DataciteServiceConfiguration,
                        )

object Configuration {

  def apply(home: File): Configuration = {
    val cfgPath = Seq(
      root / "etc" / "opt" / "dans.knaw.nl" / "easy-deposit-properties",
      home / "cfg")
      .find(_.exists)
      .getOrElse { throw new IllegalStateException("No configuration directory found") }
    val properties = new PropertiesConfiguration() {
      setDelimiterParsingDisabled(true)
      load((cfgPath / "application.properties").toJava)
    }

    new Configuration(
      version = (home / "bin" / "version").contentAsString.stripLineEnd,
      serverPort = properties.getInt("deposit-properties.daemon.http.port"),
      databaseConfig = DatabaseConfiguration(
        properties.getString("deposit-properties.database.driver-class"),
        properties.getString("deposit-properties.database.url"),
        properties.getString("deposit-properties.database.username"),
        properties.getString("deposit-properties.database.password"),
      ),
      dataciteConfig = new DataciteServiceConfiguration {
        setConnectionTimeout(properties.getString("deposit-properties.datacite.connection-timeout").toInt)
        setReadTimeout(properties.getString("deposit-properties.datacite.read-timeout").toInt)
        setUsername(properties.getString("deposit-properties.datacite.username"))
        setPassword(properties.getString("deposit-properties.datacite.password"))
        setDoiRegistrationUri(properties.getString("deposit-properties.datacite.registration.doi.uri"))
        setDatasetResolver(new URL(properties.getString("deposit-properties.datacite.resolver")))
      }
    )
  }
}
