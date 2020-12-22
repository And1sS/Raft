package com.and1ss.raft
package states

import java.util.UUID
import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}

class FollowerState(var leaderNodeId: UUID, node: Node) extends State(node) {
  private val scheduledExecutorService = Executors.newScheduledThreadPool(1)
  private var future: ScheduledFuture[_] = null

  override def init(): Unit = {
    resetElectionTimer()
  }

  private def resetElectionTimer(): Unit = {
    if (future != null) future.cancel(true)

    future = scheduledExecutorService.scheduleAtFixedRate(
      () => processLeaderElectionTimeout(),
      node.electionTimeout,
      node.electionTimeout,
      TimeUnit.MILLISECONDS
    )
  }

  override def voteForLeader(candidateNodeId: UUID, candidateTerm: Long): Boolean =
    node.synchronized {
      if (candidateTerm > node.term.get()) {
        leaderNodeId = candidateNodeId
        node.term.set(candidateTerm)
        return true
      }
      false
    }

  override def processAcknowledge(nodeId: UUID, nodeTerm: Long): Unit =
    node.synchronized {
      if (nodeTerm >= node.term.get() && nodeId.equals(leaderNodeId)) {
        println(s"Node: ${node.id}, processed acknowledge packet.")
        resetElectionTimer()
      }
    }

  private def processLeaderElectionTimeout(): Unit = this.synchronized {
    println(s"Node ${node.id} processed leader election timeout")
    future.cancel(true)
    val candidateState = new CandidateState(node)
    node.transitToState(candidateState)
    candidateState.init()
  }

  override def toString: String = "Follower state"

  override def prepareToSwitchState(): Unit = {}
}
