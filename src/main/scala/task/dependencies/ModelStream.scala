package task.dependencies

import task.models.ModelRecord
import zio.clock.Clock
import zio.stream.ZStream
import zio.{Has, ZIO}

object ModelStream {
  type ModelService = Has[Model]

  trait Model {
    def stream(): ZIO[ModelService, Throwable, ZStream[Clock, Nothing, ModelRecord]]
  }

  val stream: ZIO[ModelService, Throwable, ZStream[Clock, Nothing, ModelRecord]] = ZIO.accessM[ModelService](_.get.stream())
}
