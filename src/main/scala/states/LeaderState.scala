package com.and1ss.raft
package states

import com.and1ss.raft.connectors.Connector

import java.util.UUID
import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}

class LeaderState(node: Node) extends State(node) {
  private val scheduledExecutorService = Executors.newScheduledThreadPool(1)
  private var future: ScheduledFuture[_] = null

  override def init(): Unit = {
    future = scheduledExecutorService.scheduleAtFixedRate(
      () => node.synchronized { Connector.sendAcknowledge(node) }, 0, 10, TimeUnit.MILLISECONDS)
  }

  override def voteForLeader(candidateNodeId: UUID, candidateTerm: Long): Boolean =
    node.synchronized {
      if (candidateTerm > node.term.get()) {
        node.term.set(candidateTerm)
        prepareToSwitchState()
        val followerState = new FollowerState(candidateNodeId, node)
        node.transitToState(followerState)
        followerState.init()

        return true
      }
      false
    }

  override def prepareToSwitchState(): Unit = future.cancel(true)

  override def toString: String = "Leader state"
}
