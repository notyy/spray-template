package sample

import scala.tools.nsc.interpreter.ProcessResult

object Transfer {
  type ErrorMsg = String
  type SuccessMessage = String

  trait Request {
    def id: Int
  }

  trait ProcessResult {
    def id: Int
  }

  trait SuccessResult extends ProcessResult

  trait FailureResult extends ProcessResult {
    def msg: String
  }

  case class TransferRequest(id: Int, from: Account, to: Account, amount: Double) extends Request

  case class TransferSuccess(id: Int) extends ProcessResult

  case class TransferFailed(id: Int, message: String) extends ProcessResult


  def transfer: TransferRequest => ProcessResult = {
    case TransferRequest(id, from, to, amount) =>
      if (amount > from.balance) {
        TransferFailed(id, "not enough balance")
      } else {
        from.balance -= amount
        to.balance += amount
        TransferSuccess(id)
      }
  }
}
