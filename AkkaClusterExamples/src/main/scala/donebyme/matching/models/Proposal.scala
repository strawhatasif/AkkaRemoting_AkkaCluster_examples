package donebyme.matching.models

import java.util.{Date, UUID}

import akka.actor._
import akka.pattern.ask
import akka.actor.ActorRef
import akka.persistence.PersistentActor
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._


object ProposalClient {
  def apply(region: ActorRef): ShardProposalClient = {
    ShardProposalClient(region, nextId)
  }

  def apply(region: ActorRef, existingProposalId: String): ShardProposalClient = {
    ShardProposalClient(region, existingProposalId)
  }

  implicit val timeout = Timeout(5.seconds)

  case class ShardProposalClient(region: ActorRef, proposalId: String) {
    def submit(description: String, price: Long, expectedCompletion: Date): Future[SubmitProposalResult] = {
      (region ? CommandEnvelope(proposalId, SubmitProposal(description, price, expectedCompletion))).mapTo[SubmitProposalResult]
    }

    def resubmit(description: String, price: Long, expectedCompletion: Date): Future[ResubmitProposalResult] = {
      (region ? CommandEnvelope(proposalId, ResubmitProposal(description, price, expectedCompletion))).mapTo[ResubmitProposalResult]
    }
  }

  private def nextId: String = UUID.randomUUID.toString
}

class Proposal extends PersistentActor {
  private val proposalId = self.path.name // name must be aggregate unique id

  override def persistenceId: String = proposalId

  var state = ProposalState.nothing

  override def receiveCommand: Receive = {
    case command: SubmitProposal =>
      persist(ProposalSubmitted(proposalId, command.description, command.price, command.expectedCompletion)) { event =>
        state = realized(event)
        sender ! SubmitProposalResult(proposalId, event.description, event.price, event.expectedCompletion)
      }
    case command: ResubmitProposal =>
      persist(ProposalResubmitted(proposalId, command.description, command.price, command.expectedCompletion)) { event =>
        state = realized(event)
        sender ! ResubmitProposalResult(proposalId, event.description, event.price, event.expectedCompletion)
      }
  }

  override def receiveRecover: Receive = {
    case event: ProposalSubmitted =>
      state = realized(event)
    case event: ProposalResubmitted =>
      state = realized(event)
  }

  private def realized(event: ProposalResubmitted): ProposalState = {
    ProposalState.resubmittedWith(event.description, event.price, event.expectedCompletion)
  }

  private def realized(event: ProposalSubmitted): ProposalState = {
    ProposalState.submittedAs(event.description, event.price, event.expectedCompletion)
  }
}

object ProposalState {
  def nothing = ProposalState("", 0, new Date())

  def apply(description: String, price: Long, expectedCompletion: Date): ProposalState =
    new ProposalState(description, price, expectedCompletion)

  def resubmittedWith(description: String, price: Long, expectedCompletion: Date): ProposalState =
    ProposalState(description, price, expectedCompletion)

  def submittedAs(description: String, price: Long, expectedCompletion: Date): ProposalState =
    ProposalState(description, price, expectedCompletion)
}

case class ProposalState(description: String, price: Long, expectedCompletion: Date, suggestedPrice: Long) {
  def this(description: String, price: Long, expectedCompletion: Date) =
    this(description, price, expectedCompletion, 0)

  def rejectedBecause(suggestedPrice: Long) = {
    ProposalState(this.description, this.price, this.expectedCompletion, suggestedPrice)
  }
}
