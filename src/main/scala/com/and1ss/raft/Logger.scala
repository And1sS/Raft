package com.and1ss.raft

object Logger {
  private var _log = ""

  def log(string: String) = _log += string + "\n"

  def getLog() = _log
}
