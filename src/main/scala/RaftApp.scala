package com.and1ss.raft

import controllers.MainController

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http

import scala.io.StdIn


object RaftApp {
  def main(args: Array[String]): Unit = {
    //    Connector.printNodesState()
    //    while (true) {
    //      Thread.sleep(1000)
    //    }
    //    val response = post("http://google.com", "test")
    //    println(response.getStatusLine.getStatusCode)
    //    val str = EntityUtils.toString(response.getEntity,"UTF-8")
    //    println(str)

    implicit val system = ActorSystem(Behaviors.empty, "Raft-node")
    implicit val executionContext = system.executionContext

    val route = MainController.employeeRoutes

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
