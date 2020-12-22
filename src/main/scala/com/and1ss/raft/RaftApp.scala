package com.and1ss.raft

import com.and1ss.raft.connectors.Connector
import com.and1ss.raft.controllers.MainController

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import connectors.Connector

import scala.io.StdIn

object RaftApp {
  def main(args: Array[String]): Unit = {
    Connector.nodesPorts.foreach(println)
    implicit val system = ActorSystem(Behaviors.empty, "Raft-node")
    implicit val executionContext = system.executionContext

    val route = MainController.employeeRoutes
    val port = 8090
    val bindingFuture = Http().newServerAt("localhost", port).bind(route)

    println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
