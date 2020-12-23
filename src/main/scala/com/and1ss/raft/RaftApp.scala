package com.and1ss.raft

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.and1ss.raft.connectors.Connector
import com.and1ss.raft.controllers.MainController

import scala.io.StdIn

object RaftApp {
  private def remove(num: Int, list: List[Int]) = list diff List(num)

  implicit val system = ActorSystem(Behaviors.empty, "Raft-node")
  implicit val executionContext = system.executionContext

  def main(args: Array[String]): Unit = {
    val port = 8090

    val ports: List[Int] = (8090 to 8100).toList
    Connector.nodesPorts = remove(port, ports)
    Connector.nodesPorts.foreach(println)

    val route = MainController.employeeRoutes
    val bindingFuture = Http().newServerAt("localhost", port).bind(route)

    println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
