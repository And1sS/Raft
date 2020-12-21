package com.and1ss.raft
package states

import connectors.Connector

import java.util.UUID
import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger

class CandidateState(node: Node) extends State(node) {
//  private val votes = new AtomicInteger(0)
//  private val lastVotingTerm = node.term

  private val scheduledExecutorService = Executors.newScheduledThreadPool(1)
  private var future: ScheduledFuture[_] = null

  override def init(): Unit = {
    resetElectionTimer()
    initiateLeaderElection()
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

  override def voteForLeader(candidateNodeId: UUID, candidateTerm: Long): Boolean = {
    if (candidateTerm > node.term.get()) {
      node.term.set(candidateTerm)
      future.cancel(true)

      val followerState = new FollowerState(candidateNodeId, node)
      node.transitToState(followerState)
      followerState.init()

      return true
    }
    false
  }

  private def processLeaderElectionTimeout(): Unit = {
    println(s"Node ${node.id} processed leader election timeout")
    resetElectionTimer()
    initiateLeaderElection()
  }

  private def initiateLeaderElection(): Unit = {
    node.term.incrementAndGet()
    if (Connector.initiateLeaderElection(node)) {
      future.cancel(true)
      val leaderState = new LeaderState(node)
      node.transitToState(leaderState)
      leaderState.init()
    }
  }

  override def prepareToSwitchState(): Unit = future.cancel(true)

  override def toString: String = "Candidate state"
}