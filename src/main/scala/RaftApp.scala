package com.and1ss.raft

import connectors.Connector

object RaftApp {
  def main(args: Array[String]): Unit = {
    Connector.printNodesState()
    while (true) {
      Thread.sleep(1000)
    }
  }
}
