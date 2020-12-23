package com.and1ss.raft
package states

import java.util.UUID

abstract class State(val node: Node) {
  def init()
  def voteForLeader(candidateNodeId: UUID, candidateTerm: Long): Boolean
  def prepareToSwitchState()

  def processAcknowledge(nodeId: UUID, nodeTerm: Long): Unit = {
    if (nodeTerm > node.term.get()) {
      Logger.log(s"Node: ${node.id}, processed acknowledge packet.")
      prepareToSwitchState()
      node.term.set(nodeTerm)

      val followerState = new FollowerState(nodeId, node)
      node.transitToState(followerState)
      followerState.init()
    }
  }

  def saveMessage(message: String): Boolean = false

  def processReplicationRequest(message: String): Boolean = {
    Logger.log(s"Processing replication request $message")
    node.replicationLog = node.replicationLog :+ message
    true
  }
}

