package sample

object Transfer {
  type ErrorMsg = String
  type SuccessMessage = String

  case class TransferRequest(id: Int, from: Account, to: Account, amount: Double)
  case class TransferResult(id: Int, result: Either[ErrorMsg, SuccessMessage])

  def transfer: TransferRequest => TransferResult =  {
    case TransferRequest(id, from, to, amount) =>
      from.balance -= amount
      to.balance += amount
      TransferResult(id, Right("transfer successfully"))
  }
}
