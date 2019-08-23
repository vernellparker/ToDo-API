package com.example

import com.example.TaskRegistryActor.ActionPerformed


//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val taskJsonFormat = jsonFormat3(Task)
  implicit val tasksJsonFormat = jsonFormat1(Tasks)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-support
