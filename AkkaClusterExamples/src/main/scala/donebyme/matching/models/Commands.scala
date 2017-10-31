package donebyme.matching.models

import java.util.Date

final case class CommandEnvelope(id: String, command: Any)

// commands
case class SubmitProposal(description: String, price: Long, expectedCompletion: Date)
case class ResubmitProposal(description: String, price: Long, expectedCompletion: Date)

// results
case class SubmitProposalResult(proposalId: String, description: String, price: Long, expectedCompletion: Date)
case class ResubmitProposalResult(proposalId: String, description: String, price: Long, expectedCompletion: Date)
