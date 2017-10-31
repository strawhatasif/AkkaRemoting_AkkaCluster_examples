package donebyme.matching.infra

import akka.actor._
import akka.cluster.sharding._
import donebyme.matching.models.{CommandEnvelope, Proposal}

object ProposalShardManager {
  private var regionRef: ActorRef = _

  def region: ActorRef = regionRef

  def start(system: ActorSystem): ActorRef = {
    regionRef = ClusterSharding(system).start(
        typeName = shardTypeName,
        entityProps = Props[Proposal],
        settings = ClusterShardingSettings(system),
        extractEntityId = extractEntityId,
        extractShardId = extractShardId)

    regionRef
  }
  
  private val shardTypeName = "Proposals"
  
  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case CommandEnvelope(id, command) => (id.toString, command)
  }
 
  private val numberOfShards = 30
 
  private val extractShardId: ShardRegion.ExtractShardId = {
    case CommandEnvelope(id, _) => (Math.abs(id.hashCode) % numberOfShards).toString
  }
}
