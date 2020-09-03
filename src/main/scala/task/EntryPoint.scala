package task

import task.dependencies.{DataStream, LoggingEnv, ModelStream, Streams}
import task.models.ModelRecord
import task.processing.Processor
import zio.clock.Clock
import zio.logging.{Logging, log}
import zio._

object EntryPoint extends App {

  val process: ZIO[Logging with Clock with DataStream.DataService with ModelStream.ModelService, Throwable, Unit] = for {
    _ <- log.info(s"Start processing ... ")
    _ <- startProcessing()
  } yield ()

  def startProcessing(): ZIO[Logging with Clock with DataStream.DataService with ModelStream.ModelService, Throwable, Unit] = (for {
    modelStream <- ModelStream.stream
    _ <- log.info("Try to get first model ...")
    firstModelM <- modelStream.runHead
    firstModel <- ZIO.fromEither(firstModelM.toRight(new IllegalStateException("The Model hasn't arrived yet ... ")))
    _ <- log.info(s"Got first model $firstModel ...")
    // It's a `ZRef` instance that is used as communication point between two streams.
    // Model Stream stores a new model there, and Data stream will it as soon as a new value appears there.
    // ZRef provides the functional equivalent of a mutable reference.
    // For performance reasons `ZRef` is implemented in terms of CAS operations.
    // It's safe to use that approach if the value inside the `ZRef` is immutable.
    ref: Ref[ModelRecord] <- Ref.make(firstModel)
    _ <- log.info(s"Created ref ...")
    dataStream <- DataStream.stream
    _ <- (for {
      _ <- Processor.processData(ref, dataStream).runDrain.toManaged_.fork
      _ <- modelStream.mapM(m => log.info(s"New model has arrived : $m ") *> ref.set(m)).runDrain.toManaged_.fork
    } yield ()).useForever
  } yield ())

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] = {
    process.map(_ => 0).catchAll(e => log.error(e.getMessage) *> IO.succeed(1)).map(ExitCode(_))
      .provideSomeLayer(Clock.live ++ LoggingEnv.live >+> Streams.dataStream ++ Streams.modelStream)
  }
}
