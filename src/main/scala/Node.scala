package com.and1ss.raft

import com.and1ss.raft.states.{FollowerState, State}

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import scala.util.Random

class Node {
  var term: AtomicLong = new AtomicLong(0)
  val id: UUID = UUID.randomUUID()
  var state: State = new FollowerState(leaderNodeId = null, node = this)
  var electionTimeout: Int = new Random().nextInt(1500) + 1500

  state.init()

  def transitToState(state: State): Unit = this.synchronized {
    this.state = state
    println(s"Node $id transiting to state: $state")
  }

  def voteForLeader(candidateNodeId: UUID, candidateTerm: Long): Boolean =
    this.synchronized {
      state.voteForLeader(candidateNodeId, candidateTerm)
    }

  def processAcknowledge(nodeId: UUID, nodeTerm: Long): Unit = {
    this.synchronized {
      state.processAcknowledge(nodeId, nodeTerm)
    }
  }
}
