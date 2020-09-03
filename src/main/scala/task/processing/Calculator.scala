package task.processing

import task.models.{Accuracy, DataRecord, ModelRecord, Negative, OutputRecord, Positive}
import zio.{Chunk, Task, ZIO}
import zio.logging.{Logging, log}

import scala.math.BigDecimal
import scala.math.BigDecimal.RoundingMode
import scala.util.Try

object Calculator {

  def calculateAccuracy(chunk: Chunk[OutputRecord]): ZIO[Logging, Throwable, Option[Accuracy]] = chunk.toList match {
    case Nil =>
      log.debug("Empty chunk arrived.") *> ZIO.succeed(None)
    case list@h :: _ if list.exists(_.modelId != h.modelId) =>
      log.warn("Accuracy can't be properly calculated since chunk contains predictions from different models.") *> ZIO.succeed(None)
    case _ =>
      ZIO.succeed {
        val (truePositives, trueNegatives) = chunk.foldLeft((0, 0)) { (acc, v) =>
          (v.dataLabel, v.predictedLabel) match {
            case (Positive, Positive) => (acc._1 + 1, acc._2)
            case (Negative, Negative) => (acc._1, acc._2 + 1)
            case _ => acc
          }
        }
        Some(Accuracy((BigDecimal(truePositives + trueNegatives) / chunk.size).setScale(2, RoundingMode.HALF_EVEN), chunk.head.modelId))
      }
  }

  def predict(model: ModelRecord, data: DataRecord): Task[OutputRecord] = ZIO.fromTry {
    val features = data.features
    val logOdds = ((model.weight1 * features.x1) + (model.weight2 * features.x2) +
      (model.weight3 * features.x3) + (model.weight4 * features.x4) +
      (model.weight5 * features.x5) + model.bias).setScale(2, RoundingMode.HALF_EVEN)

    for {
      odds <- Try(BigDecimal(Math.exp(logOdds.doubleValue)))
      probability = odds / (odds + 1)
      prediction = if (probability > 0.5) Positive else Negative
    } yield OutputRecord(data.classLabel, prediction, model.id)
  }
}
