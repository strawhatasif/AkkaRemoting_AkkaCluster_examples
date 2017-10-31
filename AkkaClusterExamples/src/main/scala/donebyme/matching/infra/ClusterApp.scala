package donebyme.matching.infra

import java.util.Date

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import donebyme.matching.models.ProposalClient

import scala.concurrent.ExecutionContext.Implicits.global

object ClusterApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2551", "2552", "0"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)
      // Create an actor that handles cluster domain events
      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")

      ProposalShardManager.start(system)

      println(s"REGISTERED PROPOSAL SHARD REGION ON SYSTEM: $system")

      Thread.sleep(1000L)

      val twoDaysFromNow = new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 2))

      val proposal = ProposalClient(ProposalShardManager.region)

      proposal.submit("Proposal text description", 199599, twoDaysFromNow).map { result =>
        println(s"""  CREATED AND SUBMITTED PROPOSAL IN SHARD REGION:
                    |    ID=${result.proposalId}
                    |    DESCRIPTION=${result.description}
                    |    PRICE=${result.price}
                    |    EXPECTED_BY=${result.expectedCompletion}""".stripMargin)
      }

      Thread.sleep(1000L)

      proposal.resubmit("Proposal text description (edited)", 219599, twoDaysFromNow).map { result =>
        println(s"""  RESUBMITTED EXISTING PROPOSAL IN SHARD REGION:
                   |    ID=${result.proposalId}
                   |    DESCRIPTION=${result.description}
                   |    PRICE=${result.price}
                   |    EXPECTED_BY=${result.expectedCompletion}""".stripMargin)
      }

      Thread.sleep(3000L)
    }
  }
}
