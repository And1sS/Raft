package com.and1ss.raft
package controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol.{BooleanJsonFormat, LongJsonFormat, StringJsonFormat, jsonFormat1, jsonFormat2}

import java.util.UUID

object MainController {
  private val node = new Node

  final case class LeaderElectionRequestDTO(nodeId: String, term: Long)

  final case class LeaderElectionResponseDTO(result: Boolean)

  final case class Acknowledge(nodeId: String, term: Long)

  implicit val leRequestFormat = jsonFormat2(LeaderElectionRequestDTO)
  implicit val leResponseFormat = jsonFormat1(LeaderElectionResponseDTO)
  implicit val acknowledgeFormat = jsonFormat2(Acknowledge)

  lazy val employeeRoutes: Route = concat(
    path("leader-election") {
      put {
        entity(as[LeaderElectionRequestDTO]) {
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
    }
  )

  private def processLeaderElectionRequest(leaderElectionRequestDTO: LeaderElectionRequestDTO)
  : StandardRoute = {
    try {
      val nodeId = UUID.fromString(leaderElectionRequestDTO.nodeId)
      val nodeTerm = leaderElectionRequestDTO.term
      val result = node.voteForLeader(nodeId, nodeTerm)
      complete(StatusCodes.OK, LeaderElectionResponseDTO(result))
    } catch {
      case _ => complete(StatusCodes.OK, LeaderElectionResponseDTO(false))
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
}
