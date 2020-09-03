package task.models

import java.util.UUID

sealed trait ClassLabel
case object Positive extends ClassLabel
case object Negative extends ClassLabel

final case class Features(
  x1: BigDecimal,
  x2: BigDecimal,
  x3: BigDecimal,
  x4: BigDecimal,
  x5: BigDecimal
)

final case class DataRecord(
  features: Features,
  classLabel: ClassLabel
)

final case class ModelRecord(
  weight1: BigDecimal,
  weight2: BigDecimal,
  weight3: BigDecimal,
  weight4: BigDecimal,
  weight5: BigDecimal,
  bias: BigDecimal,
  id: UUID = UUID.randomUUID()
)

final case class OutputRecord(
  dataLabel: ClassLabel,
  predictedLabel: ClassLabel,
  modelId: UUID
)

final case class Accuracy(value: BigDecimal, modelId: UUID)
