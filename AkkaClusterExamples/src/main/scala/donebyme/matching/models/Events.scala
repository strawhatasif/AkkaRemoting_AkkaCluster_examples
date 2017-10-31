package donebyme.matching.models

import java.util.Date

trait DomainEvent {
  def eventVersion: Int
  def occurredOn: Date
}

case class ProposalSubmitted(proposalId: String, description: String, price: Long, expectedCompletion: Date) extends DomainEvent {
  val eventVersion: Int = 1
  val occurredOn: Date = new Date()
}

case class ProposalResubmitted(proposalId: String, description: String, price: Long, expectedCompletion: Date) extends DomainEvent {
  val eventVersion: Int = 1
  val occurredOn: Date = new Date()
}
