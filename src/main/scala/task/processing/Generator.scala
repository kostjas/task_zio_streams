package task.processing

import task.models.{DataRecord, Features, Negative, Positive}

import scala.math.BigDecimal.RoundingMode
import scala.util.Random

object Generator {
  def generateFeature(from: Double, to: Double): BigDecimal = BigDecimal(Random.between(from, to)).setScale(2, RoundingMode.HALF_EVEN)

  def dataRecord(): DataRecord = {
    val features = Features(
      x1 = generateFeature(-15, 15),
      x2 = generateFeature(-10, 10),
      x3 = generateFeature(-20, 20),
      x4 = generateFeature(-30, 30),
      x5 = generateFeature(-5, 5)
    )
    DataRecord(
      features = features,
      classLabel = if (features.x1 + features.x2 + features.x3 + features.x4 + features.x5 > 0) Positive else Negative
    )
  }
}
