package task.processing

import java.util.UUID

import task.models.{ClassLabel, DataRecord, Features, ModelRecord, Negative, OutputRecord, Positive}
import zio.Chunk
import zio.random.Random
import zio.test.Gen.{boolean, chunkOf, double}
import zio.test.{Gen, Sized}

object Generators {

  def genFeatureValue(from: Double, to : Double): Gen[Random with Sized, BigDecimal] = double(from, to).map(BigDecimal.apply)

  val genFeatures: Gen[Random with Sized, Features] = for {
    x1 <- genFeatureValue(-80.0D, 80.0D)
    x2 <- genFeatureValue(-30.0D, 30.0D)
    x3 <- genFeatureValue(-10.0D, 10.0D)
    x4 <- genFeatureValue(-40.0D, 40.0D)
    x5 <- genFeatureValue(-60.0D, 60.0D)
  } yield Features(x1, x2, x3, x4, x5)

  val dataRecordGen: Gen[Random with Sized, DataRecord] = for {
    features <- genFeatures
    classLabel = if (features.x1 + features.x2 + features.x3 + features.x4 + features.x5 > 0) Positive else Negative
  } yield DataRecord(features, classLabel)

  private val models = List(
    ModelRecord(BigDecimal(6.08D), BigDecimal(7.78D), BigDecimal(6.34D), BigDecimal(8.05D), BigDecimal(3.14D), BigDecimal(61.35)),
    ModelRecord(BigDecimal(8.46D), BigDecimal(1.74D), BigDecimal(6.08D), BigDecimal(4.25D), BigDecimal(1.92D), BigDecimal(71.37)),
    ModelRecord(BigDecimal(6.53D), BigDecimal(5.46D), BigDecimal(0.0D), BigDecimal(9.95D), BigDecimal(6.29D), BigDecimal(43.3)),
    ModelRecord(BigDecimal(3.2D), BigDecimal(7.32D), BigDecimal(1.46D), BigDecimal(2.29D), BigDecimal(4.26D), BigDecimal(94.81)),
    ModelRecord(BigDecimal(2.71D), BigDecimal(0.82D), BigDecimal(8.54D), BigDecimal(0.21D), BigDecimal(2.1D), BigDecimal(66.25))
  )

  val modelRecordGen: Gen[Random with Sized, ModelRecord] = Gen.oneOf(Gen.fromIterable(models))


  val labelGen: Gen[Random, ClassLabel] = boolean.map { if (_) Positive else Negative }

  val outputRecordGen: Gen[Random with Sized, OutputRecord] = for {
    label <- labelGen
    predictedLabel <- labelGen
  } yield OutputRecord(label, predictedLabel, UUID.randomUUID())

  val outputRecordChunk: Gen[Random with Sized, Chunk[OutputRecord]] = chunkOf(outputRecordGen)
}
