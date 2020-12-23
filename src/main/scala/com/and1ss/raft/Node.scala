package com.and1ss.raft

import com.and1ss.raft.states.{FollowerState, State}

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import scala.util.Random

class Node {
  var term: AtomicLong = new AtomicLong(0)
  val id: UUID = UUID.randomUUID()
  var electionTimeout: Int = new Random().nextInt(1500) + 1500
  var state: State = new FollowerState(leaderNodeId = null, node = this)

  var replicationLog: List[String] = List()

  state.init()

  def transitToState(state: State): Unit = {
    this.state = state
    Logger.log(s"Node $id transiting to state: $state")
  }

  def voteForLeader(candidateNodeId: UUID, candidateTerm: Long): Boolean = {
    Logger.log(s"Vote for leader $candidateNodeId, $candidateTerm. Ours: $id, ${term.get()}")
      state.voteForLeader(candidateNodeId, candidateTerm)
    }

  def processAcknowledge(nodeId: UUID, nodeTerm: Long): Unit =
      state.processAcknowledge(nodeId, nodeTerm)

  def saveMessage(message: String): Boolean =
    state.saveMessage(message)

  def processReplicationRequest(message: String): Boolean =
    state.processReplicationRequest(message)
}
