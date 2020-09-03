package task.processing

import java.util.concurrent.TimeUnit

import task.dependencies.LoggingEnv
import task.models.ModelRecord
import zio.{Ref, UIO}
import zio.clock.Clock
import zio.duration.Duration
import zio.test.{DefaultRunnableSpec, ZSpec, _}
import zio.test.Assertion._
import zio.stream.ZStream
import zio.test.environment.TestClock
import zio.test.environment.TestEnvironment

import scala.concurrent.duration._

object ProcessorSpec extends DefaultRunnableSpec {

  private val testModels = List(
    ModelRecord(BigDecimal(6.08D), BigDecimal(7.78D), BigDecimal(6.34D), BigDecimal(8.05D), BigDecimal(3.14D), BigDecimal(61.35)),
    ModelRecord(BigDecimal(8.46D), BigDecimal(1.74D), BigDecimal(6.08D), BigDecimal(4.25D), BigDecimal(1.92D), BigDecimal(71.37)),
    ModelRecord(BigDecimal(6.53D), BigDecimal(5.46D), BigDecimal(0.0D), BigDecimal(9.95D), BigDecimal(6.29D), BigDecimal(43.3)),
    ModelRecord(BigDecimal(3.2D), BigDecimal(7.32D), BigDecimal(1.46D), BigDecimal(2.29D), BigDecimal(4.26D), BigDecimal(94.81)),
    ModelRecord(BigDecimal(2.71D), BigDecimal(0.82D), BigDecimal(8.54D), BigDecimal(0.21D), BigDecimal(2.1D), BigDecimal(66.25))
  )

  private val suites = suite("Processor")(
    testM(s"should pick first model") {
      val modelRecord = ModelRecord(BigDecimal(6.08D), BigDecimal(7.78D), BigDecimal(6.34D),
        BigDecimal(8.05D), BigDecimal(3.14D), BigDecimal(61.35))
      val modelStream = ZStream(modelRecord)
      modelStream.runHead.map { model => assert(model)(isSome(equalTo(modelRecord)))}
    },
    testM(s"should wait for the first model if it's not present yet") {
      for {
        fork <- ZStream.never.runHead.timeout(Duration.fromScala(3.seconds)).fork
        _ <- TestClock.adjust(Duration.fromScala(3.seconds))
        result <- fork.join
      } yield assert(result)(isNone)
    },
    testM(s"should process data") {
      val initialModel = testModels.head
      val ref: UIO[Ref[ModelRecord]] = Ref.make(initialModel)

      val modelStream: ZStream[Clock, Nothing, ModelRecord] = ZStream.tick(Duration(100, TimeUnit.MILLISECONDS))
        .mapAccum(testModels.tail)( (models, _) => (if (models.isEmpty) Nil else models.tail, models.headOption.toList))
        .mapConcat(identity)
        .mapM(m => ref.map(_.set(m)).map(_ => m))

      val dataStream = ZStream.fromIterable((0 to 1000).map(_ => Generator.dataRecord()))

      for {
        r <- ref
        chunk <- (for {
          _ <- modelStream.runDrain.toManaged_.fork
          result <- Processor.processData(r, dataStream).runCollect.toManaged_.fork
        } yield result).use(_.join)
      } yield assert(chunk)(isNonEmpty)
    },
  ).provideCustomLayer(TestEnvironment.any ++ LoggingEnv.live)

  override def spec: ZSpec[TestEnvironment, Throwable] = suites
}
