package task.processing

import task.models.{Accuracy, DataRecord, ModelRecord}
import task.processing.Calculator.calculateAccuracy
import zio.{Chunk, Ref}
import zio.stream.ZStream
import zio.clock.Clock
import zio.logging.{Logging, log}

object Processor {

  def processData(ref: Ref[ModelRecord], dataStream: ZStream[Clock, Throwable, DataRecord]): ZStream[Logging with Clock, Throwable, Unit] =
    dataStream.mapM(data => ref.get.flatMap ( model =>
      Calculator.predict(model, data).tapError(e => log.error(s"Probability cannot be calculated ${e.getMessage}"))
    )).chunkN(10).mapChunksM[Logging with Clock, Throwable, Accuracy] ( chunk =>
        calculateAccuracy(chunk).map(_.fold[Chunk[Accuracy]](Chunk())(a => Chunk(a)))
      ).mapM ( accuracy => log.info(s"Accuracy of the model : ${accuracy.modelId} : ${accuracy.value}"))
}