package task.dependencies

import java.util.concurrent.TimeUnit

import task.models.ModelRecord
import task.processing.Generator
import zio.duration.Duration
import zio.logging.{Logger, Logging}
import zio.stream.ZStream
import zio.{Has, ZLayer}

import scala.util.Random

object Streams {

  val dataStream: ZLayer[Logging, Nothing, Has[DataStream.Data]] =
    ZLayer.fromService[Logger[String], DataStream.Data](logger => () => for {
    _ <- logger.info("Creating Data Stream ...")
    dataStream = ZStream.tick(Duration(100, TimeUnit.MILLISECONDS)).map(_ => Generator.dataRecord())
  } yield dataStream)

  val modelStream: ZLayer[Logging, Nothing, Has[ModelStream.Model]] =
    ZLayer.fromService[Logger[String], ModelStream.Model](logger => () => for {
      _ <- logger.info("Creating Model Stream ...")
      modelStream = ZStream.tick(Duration(5, TimeUnit.SECONDS)).map(_ => models(Random.nextInt(4)))
    } yield modelStream
  )

  private val models = List(
    ModelRecord(BigDecimal(6.08D), BigDecimal(7.78D), BigDecimal(6.34D), BigDecimal(8.05D), BigDecimal(3.14D), BigDecimal(61.35)),
    ModelRecord(BigDecimal(8.46D), BigDecimal(1.74D), BigDecimal(6.08D), BigDecimal(4.25D), BigDecimal(1.92D), BigDecimal(71.37)),
    ModelRecord(BigDecimal(6.53D), BigDecimal(5.46D), BigDecimal(0.0D), BigDecimal(9.95D), BigDecimal(6.29D), BigDecimal(43.3)),
    ModelRecord(BigDecimal(3.2D), BigDecimal(7.32D), BigDecimal(1.46D), BigDecimal(2.29D), BigDecimal(4.26D), BigDecimal(94.81)),
    ModelRecord(BigDecimal(2.71D), BigDecimal(0.82D), BigDecimal(8.54D), BigDecimal(0.21D), BigDecimal(2.1D), BigDecimal(66.25))
  )
}
