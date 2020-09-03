package task.processing

import java.util.UUID

import task.dependencies.LoggingEnv
import task.models.{Accuracy, DataRecord, Features, ModelRecord, Negative, OutputRecord, Positive}
import zio.Chunk
import zio.test.DefaultRunnableSpec
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

object CalculatorSpec extends DefaultRunnableSpec {

  private val suites = suite("Processor")(
    testM(s"should calculate properly Negative case conditional probability") {
      val model = ModelRecord(BigDecimal(2.71D), BigDecimal(0.82D), BigDecimal(8.54D), BigDecimal(0.21D), BigDecimal(2.1D), BigDecimal(66.25))
      val data = DataRecord(Features(BigDecimal(-10.0D), BigDecimal(-20.0D), BigDecimal(-50.0D), BigDecimal(-80.0D), BigDecimal(30.0D)), Negative)
      val output = OutputRecord(Negative, Negative, model.id)
      Calculator.predict(model, data).map { o => assert(o)(equalTo(output)) }
    },
    testM(s"should calculate properly Positive case conditional probability") {
      val model = ModelRecord(BigDecimal(3.2D), BigDecimal(7.32D), BigDecimal(1.46D), BigDecimal(2.29D), BigDecimal(4.26D), BigDecimal(94.81))
      val data = DataRecord(Features(BigDecimal(2.0D), BigDecimal(-20.0D), BigDecimal(80.0D), BigDecimal(40.0D), BigDecimal(10.0D)), Positive)
      val output = OutputRecord(Positive, Positive, model.id)
      Calculator.predict(model, data).map { o => assert(o)(equalTo(output)) }
    },
    testM(s"should fail if calculation is not possible") {
      val model = ModelRecord(BigDecimal(6.08D), BigDecimal(7.78D), BigDecimal(6.34D), BigDecimal(8.05D), BigDecimal(3.14D), BigDecimal(61.35))
      val data = DataRecord(Features(BigDecimal(Double.MaxValue), BigDecimal(Double.MaxValue), BigDecimal(Double.MaxValue), BigDecimal(Double.MaxValue), BigDecimal(Double.MaxValue)), Positive)
      assertM(Calculator.predict(model, data).run)(fails(isSubtype[NumberFormatException](anything)))
    },
    testM(s"shouldn't calculate accuracy if input chunk is empty") {
      Calculator.calculateAccuracy(Chunk()).map { o => assert(o)(isNone) }
    },
    testM(s"shouldn't calculate accuracy if chunk contains multiple models") {
      val chunk = Chunk(OutputRecord(Positive, Positive, UUID.randomUUID()), OutputRecord(Positive, Positive, UUID.randomUUID()))
      Calculator.calculateAccuracy(chunk).map { o => assert(o)(isNone) }
    },
    testM(s"shouldn't calculate accuracy properly for 100% positive cases") {
      val uuid = UUID.randomUUID()
      val chunk = Chunk(OutputRecord(Positive, Positive, uuid), OutputRecord(Positive, Positive, uuid))
      Calculator.calculateAccuracy(chunk).map { o => assert(o)(isSome(equalTo(Accuracy(BigDecimal(1.0), uuid)))) }
    },
    testM(s"shouldn't calculate accuracy properly for 100% negative cases") {
      val uuid = UUID.randomUUID()
      val chunk = Chunk(OutputRecord(Negative, Negative, uuid), OutputRecord(Negative, Negative, uuid))
      Calculator.calculateAccuracy(chunk).map { o => assert(o)(isSome(equalTo(Accuracy(BigDecimal(1.0), uuid)))) }
    },
    testM(s"shouldn't calculate accuracy properly for 0%") {
      val uuid = UUID.randomUUID()
      val chunk = Chunk(OutputRecord(Positive, Negative, uuid), OutputRecord(Positive, Negative, uuid))
      Calculator.calculateAccuracy(chunk).map { o => assert(o)(isSome(equalTo(Accuracy(BigDecimal(0), uuid)))) }
    },
    testM(s"shouldn't calculate accuracy properly for 67%") {
      val uuid = UUID.randomUUID()
      val chunk = Chunk(OutputRecord(Positive, Positive, uuid), OutputRecord(Positive, Negative, uuid), OutputRecord(Negative, Negative, uuid))
      Calculator.calculateAccuracy(chunk).map { o => assert(o)(isSome(equalTo(Accuracy(BigDecimal(0.67), uuid)))) }
    },
    testM(s"should calculate conditional probability or fails with NumberFormatException") {
      checkM(Generators.modelRecordGen, Generators.dataRecordGen) { (model, data) =>
        assertM(Calculator.predict(model, data).run)(Assertion.anything || fails(isSubtype[NumberFormatException](anything)))
      }
    },
    testM(s"should calculate accurancy without failures") {
      checkM(Generators.outputRecordChunk) { outputRecord =>
        Calculator.calculateAccuracy(outputRecord).map { o => assert(o)(Assertion.anything) }
      }
    }
  ).provideCustomLayer(TestEnvironment.live ++ LoggingEnv.live)

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] = suites
}
