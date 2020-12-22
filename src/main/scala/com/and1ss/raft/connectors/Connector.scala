package com.and1ss.raft
package connectors

import com.and1ss.raft.connectors.Connector.BooleanResponse.{BooleanResponse, False, True}
import com.and1ss.raft.controllers.MainController.LeaderElectionResponseDTO
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPut}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import spray.json._

object Connector {
  var nodesPorts: List[Int] = List[Int](8091)

  object BooleanResponse extends Enumeration {
    type BooleanResponse = Value

    val True, False, Error = Value
  }

  def putRequest(address: String, message: String)
  : CloseableHttpResponse = {
    val put = new HttpPut(address)
    val entity = new StringEntity(message)
    entity.setContentEncoding("UTF-8")
    entity.setContentType("application/json")
    put.setEntity(entity)
    val client = new DefaultHttpClient
    client.execute(put)
  }

  def voteForLeaderRequest(port: Int, node: Node): BooleanResponse = try {
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
    val leaderElectionResult = str.parseJson.convertTo[LeaderElectionResponseDTO]
    leaderElectionResult.result match {
      case true => BooleanResponse.True
      case false => BooleanResponse.False
    }
  } catch {
    case e: Exception => {
      println(s"Error with node on port $port: ${e.getMessage}")
      BooleanResponse.Error
    }
  }

  def acknowledge(port: Int, node: Node): Unit = try {
    putRequest(
      s"http://127.0.0.1:$port/acknowledge",
      s"""
         |{
         |  "nodeId": "${node.id.toString}",
         |  "term": ${node.term.get()}
         |}
      """.stripMargin
    )
  } catch {
    case e: Exception => println(s"Error with node on port $port: ${e.getMessage}")
  }

  def initiateLeaderElection(initiatorNode: Node): Boolean =
    initiatorNode.synchronized {
      println(s"Initiated leader election for ${initiatorNode.id}")
      var votes = 1
      var voted = 1
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

        if (2 * votes > voted) {
          result = true
        }
      })
      println(s"Initiated vote finished for ${initiatorNode.id} with result: $result, votes: $votes")
      result
    }

  def sendAcknowledge(initiatorNode: Node): Unit =
    initiatorNode.synchronized {
      println(s"Sent acknowledge from ${initiatorNode.id}")
      nodesPorts.foreach(nodePort => {
        acknowledge(nodePort, initiatorNode)
      })
    }
}
