package task.dependencies

import zio.ZLayer
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{LogAnnotation, Logging}

object LoggingEnv {

  private val logFormat = "[correlation-id = %s] %s"

  val live: ZLayer[Any, Nothing, Logging] = Slf4jLogger.make { (context, message) =>
    val correlationId = LogAnnotation.CorrelationId.render(context.get(LogAnnotation.CorrelationId))
    logFormat.format(correlationId, message)
  }
}
