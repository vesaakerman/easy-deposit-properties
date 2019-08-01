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
package nl.knaw.dans.easy.properties.app.database

sealed trait SQLErrorHandler {

  def isForeignKeyError(t: Throwable): Boolean

  def isUniquenessConstraintError(t: Throwable): Boolean
}

object SQLErrorHandler {
  def apply(dbConfig: DatabaseConfiguration): SQLErrorHandler = {
    dbConfig.dbDriverClassName match {
      case "org.postgresql.Driver" => PostgreSQLErrorHandler
      case "org.hsqldb.jdbcDriver" => HSQLDBErrorHandler
      case driver => throw new IllegalArgumentException(s"Driver '$driver' is not supported with the SQLErrorHandler.")
    }
  }
}

private object HSQLDBErrorHandler extends SQLErrorHandler {
  override def isForeignKeyError(t: Throwable): Boolean = {
    t.getMessage contains "constraint violation: foreign key"
  }

  override def isUniquenessConstraintError(t: Throwable): Boolean = {
    t.getMessage contains "constraint violation: unique constraint"
  }
}

private object PostgreSQLErrorHandler extends SQLErrorHandler {
  override def isForeignKeyError(t: Throwable): Boolean = {
    t.getMessage contains "violates foreign key constraint"
  }

  override def isUniquenessConstraintError(t: Throwable): Boolean = {
    t.getMessage contains "violates unique constraint"
  }
}
