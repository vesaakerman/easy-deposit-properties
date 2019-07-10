package nl.knaw.dans.easy.properties.app.graphql.middleware

import org.slf4j.Logger
import sangria.slowlog.SlowLog

import scala.concurrent.duration.FiniteDuration

object Profiling {
  def apply(logger: Logger)(config: ProfilingConfiguration): SlowLog = {
    SlowLog(logger, threshold = config.threshold, addExtensions = config.addExtensions)
  }
}

case class ProfilingConfiguration(threshold: FiniteDuration, addExtensions: Boolean)
