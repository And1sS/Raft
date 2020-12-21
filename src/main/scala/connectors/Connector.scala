package com.and1ss.raft
package connectors

object Connector {
  private val nodes = List(
    new Node(), new Node(), new Node(), new Node()
  )

  def printNodesState(): Unit = {
    println("-----------------------")
    nodes.foreach(
      node => println(s"Node ${node.id} is in ${node.state.toString}")
    )
    println("-----------------------")
  }

  def initiateLeaderElection(initiatorNode: Node): Boolean = {
    println(s"Initiated leader election for ${initiatorNode.id}")
    var votes = 1
    var result = false
    for (node <- nodes) {
      if (!initiatorNode.id.equals(node.id))  {
        if (node.voteForLeader(initiatorNode.id, initiatorNode.term.get())) {
          votes += 1
          if (2 * votes > nodes.size) {
            result = true
          }
        }
      }
    }

    println(s"Initiated vote finished for ${initiatorNode.id} with result: $result, votes: $votes")
    result
  }

  def sendAcknowledge(initiatorNode: Node): Unit = {
    println(s"Sent acknowledge from ${initiatorNode.id}")
    for (node <- nodes) {
      if (!initiatorNode.id.equals(node.id)) {
        node.processAcknowledge(initiatorNode.id, initiatorNode.term.get())
      }
    }
  }
}
