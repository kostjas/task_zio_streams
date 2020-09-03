package task.dependencies

import task.models.DataRecord
import zio.clock.Clock
import zio.stream.ZStream
import zio.{Has, ZIO}

object DataStream {
  type DataService = Has[Data]

  trait Data {
    def stream(): ZIO[DataService, Throwable, ZStream[Clock, Nothing, DataRecord]]
  }

  val stream: ZIO[DataService, Throwable, ZStream[Clock, Nothing, DataRecord]] = ZIO.accessM[DataService](_.get.stream())
}
