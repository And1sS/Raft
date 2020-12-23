package com.and1ss.raft
package connectors

import com.and1ss.raft.connectors.Connector.BooleanResponse.{BooleanResponse, False, True}
import com.and1ss.raft.controllers.MainController.BooleanResultDTO
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost, HttpPut}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import spray.json._

object Connector {
  var nodesPorts: List[Int] = List[Int]()

  object BooleanResponse extends Enumeration {
    type BooleanResponse = Value

    val True, False, Error = Value
  }

  def initiateLeaderElection(initiatorNode: Node): Boolean = {
    Logger.log(s"Initiated leader election for ${initiatorNode.id}")
    var votes = 1
    var voted = 1
    val initialTerm = initiatorNode.term.get()
    var result = false

    nodesPorts.foreach(nodePort => {
      val response = voteForLeaderRequest(nodePort, initiatorNode)
      response match {
        case False => voted += 1
        case True => {
          voted += 1
          votes += 1
        }
        case _ => ()
      }

      if (2 * votes > voted && initiatorNode.term.get() == initialTerm) {
        result = true
      }
    })
    Logger.log(s"Initiated vote finished for ${initiatorNode.id} with result: $result, votes: $votes")
    result
  }

  def sendAcknowledge(initiatorNode: Node): Unit = {
    Logger.log(s"Sent acknowledge from ${initiatorNode.id}")
    nodesPorts.foreach(nodePort => {
      acknowledgeRequest(nodePort, initiatorNode)
    })
  }

  def replicateMessage(initiatorNode: Node, message: String): Unit = {
    Logger.log(s"Initiated message replication for message: $message, Node: ${initiatorNode.id}")
    nodesPorts.foreach(nodePort => {
      replicateMessageRequest(nodePort, initiatorNode, message)
    })
  }

  private def createEntity(message: String): StringEntity = {
    val entity = new StringEntity(message)
    entity.setContentEncoding("UTF-8")
    entity.setContentType("application/json")
    entity
  }

  private def putRequest(address: String, message: String)
  : CloseableHttpResponse = {
    val put = new HttpPut(address)
    put.setEntity(createEntity(message))
    val client = new DefaultHttpClient
    client.execute(put)
  }

  private def postRequest(address: String, message: String)
  : CloseableHttpResponse = {
    val post = new HttpPost(address)
    post.setEntity(createEntity(message))
    val client = new DefaultHttpClient
    client.execute(post)
  }

  private def voteForLeaderRequest(port: Int, node: Node): BooleanResponse = try {
    val response = putRequest(
      s"http://127.0.0.1:$port/leader-election",
      s"""
         |{
         |  "nodeId": "${node.id.toString}",
         |  "term": ${node.term.get()}
         |}
      """.stripMargin
    )
    val str = EntityUtils.toString(response.getEntity, "UTF-8")
    response.close()
    val leaderElectionResult = str.parseJson.convertTo[BooleanResultDTO]
    leaderElectionResult.result match {
      case true => BooleanResponse.True
      case false => BooleanResponse.False
    }
  } catch {
    case e: Exception => {
      Logger.log(s"Error leader election request to node on port $port: ${e.getMessage}")
      BooleanResponse.Error
    }
  }

  private def acknowledgeRequest(port: Int, node: Node): Unit = try {
    putRequest(
      s"http://127.0.0.1:$port/acknowledge",
      s"""
         |{
         |  "nodeId": "${node.id.toString}",
         |  "term": ${node.term.get()}
         |}
      """.stripMargin
    ).close()
  } catch {
    case e: Exception => Logger.log(s"Error sending acknowledge to node on port $port: ${e.getMessage}")
  }

  private def replicateMessageRequest(port: Int, node: Node, message: String): Unit = try {
    postRequest(
      s"http://127.0.0.1:$port/replication-log",
      s"""
         |{
         |  "nodeId": "${node.id.toString}",
         |  "term": ${node.term.get()},
         |  "message": "$message"
         |}
      """.stripMargin
    ).close()
  } catch {
    case e: Exception => Logger.log(s"Error replicating message to node on port $port: ${e.getMessage}")
  }
}
