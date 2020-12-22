package com.and1ss.raft
package connectors

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair

import java.util

object Connector {
  def postRequest(address: String, message: String): CloseableHttpResponse = {
    val post = new HttpPost(address)
    val nameValuePairs = new util.ArrayList[NameValuePair]()
    nameValuePairs.add(new BasicNameValuePair("JSON", message))
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs))

    val client = new DefaultHttpClient
    client.execute(post)
  }

  def getRequest(address: String, message: String): CloseableHttpResponse = {
    val post = new HttpPost(address)
    val nameValuePairs = new util.ArrayList[NameValuePair]()
    nameValuePairs.add(new BasicNameValuePair("JSON", message))
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs))

    val client = new DefaultHttpClient
    client.execute(post)
  }

//  def printNodesState(): Unit = {
//    println("-----------------------")
//    nodes.foreach(node => println(s"Node ${node.id} is in ${node.state.toString}"))
//    println("-----------------------")
//  }

  def initiateLeaderElection(initiatorNode: Node): Boolean =
    initiatorNode.synchronized {
      println(s"Initiated leader election for ${initiatorNode.id}")
      var votes = 1
      var result = false
//      for (node <- nodes) {
//        if (!initiatorNode.id.equals(node.id)) {
//          if (node.voteForLeader(initiatorNode.id, initiatorNode.term.get())) {
//            votes += 1
//            if (2 * votes > nodes.size) {
//              result = true
//            }
//          }
//        }
//      }
//
      println(s"Initiated vote finished for ${initiatorNode.id} with result: $result, votes: $votes")
      result
    }

  def sendAcknowledge(initiatorNode: Node): Unit = initiatorNode.synchronized {
    println(s"Sent acknowledge from ${initiatorNode.id}")
//    for (node <- nodes) {
//      if (!initiatorNode.id.equals(node.id)) {
//        node.processAcknowledge(initiatorNode.id, initiatorNode.term.get())
//      }
//    }
  }
}
