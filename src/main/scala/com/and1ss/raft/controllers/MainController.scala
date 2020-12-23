package com.and1ss.raft.controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import com.and1ss.raft.{Logger, Node}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol.{BooleanJsonFormat, LongJsonFormat, StringJsonFormat, jsonFormat1, jsonFormat2, jsonFormat3}

import java.util.UUID

object MainController {
  private val node = new Node

  final case class NodeDataDTO(nodeId: String, term: Long)

  final case class BooleanResultDTO(result: Boolean)

  final case class Acknowledge(nodeId: String, term: Long)

  final case class MessageCreationDTO(message: String)

  final case class ReplicationRequestDTO(nodeId: String, term: Long, message: String)

  implicit val leRequestFormat = jsonFormat2(NodeDataDTO)
  implicit val leResponseFormat = jsonFormat1(BooleanResultDTO)
  implicit val messageCreationFormat = jsonFormat1(MessageCreationDTO)
  implicit val acknowledgeFormat = jsonFormat2(Acknowledge)
  implicit val replicationRequestFormat = jsonFormat3(ReplicationRequestDTO)

  lazy val employeeRoutes: Route = concat(
    path("leader-election") {
      put {
        entity(as[NodeDataDTO]) {
          processLeaderElectionRequest
        }
      }
    },
    path("acknowledge") {
      put {
        entity(as[Acknowledge]) {
          processAcknowledge
        }
      }
    },
    path("log") {
      get {
        complete(Logger.getLog())
      }
    },
    path("state") {
      get {
        complete(node.state.toString)
      }
    },
    path("messages") {
      concat(
        get {
          complete(node.replicationLog.mkString("\n"))
        },
        post {
          entity(as[MessageCreationDTO]) { messageCreationDTO =>
            processMessageCreation(messageCreationDTO.message)
          }
        }
      )
    },
    path("replication-log") {
      concat(
        post {
          entity(as[ReplicationRequestDTO]) {
            processReplicationLogRequest
          }
        },
        get {
          complete(node.replicationLog.mkString("\n"))
        }
      )
    }
  )

  private def processLeaderElectionRequest(leaderElectionRequestDTO: NodeDataDTO)
  : StandardRoute = {
    try {
      val nodeId = UUID.fromString(leaderElectionRequestDTO.nodeId)
      val nodeTerm = leaderElectionRequestDTO.term
      val result = node.voteForLeader(nodeId, nodeTerm)
      complete(StatusCodes.OK, BooleanResultDTO(result))
    } catch {
      case _ => complete(StatusCodes.OK, BooleanResultDTO(false))
    }
  }

  private def processAcknowledge(acknowledge: Acknowledge): StandardRoute = {
    try {
      val nodeId = UUID.fromString(acknowledge.nodeId)
      val nodeTerm = acknowledge.term
      node.processAcknowledge(nodeId, nodeTerm)
    } catch {
      case _ => ()
    }
    complete(StatusCodes.OK)
  }

  private def processMessageCreation(message: String): StandardRoute =
    node.saveMessage(message) match {
      case true => complete(StatusCodes.Created)
      case false => complete(StatusCodes.BadRequest, "This server does not process message creation.")
    }

  private def processReplicationLogRequest(replicationRequestDTO: ReplicationRequestDTO)
  : StandardRoute = node.processReplicationRequest(replicationRequestDTO.message) match {
    case true => complete(StatusCodes.Created)
    case false => complete(StatusCodes.BadRequest, "This server does not process log replication.")
  }
}
