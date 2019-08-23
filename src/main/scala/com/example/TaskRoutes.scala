package com.example

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import com.example.TaskRegistryActor.{ActionPerformed, CreateTask, DeleteTask, GetTask, GetTasks}

//#task-routes-class
trait TaskRoutes extends JsonSupport {
  //#task-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[TaskRoutes])

  // other dependencies that UserRoutes use
  def taskRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-routes
  lazy val taskRoutes: Route =
    pathPrefix(pm = "api"){
      pathPrefix("tasks") {
        concat(
          pathEnd {
            concat(
              get {
                //#tasks-get-all
                val tasks: Future[Tasks] =
                  (taskRegistryActor ? GetTasks).mapTo[Tasks]
                complete(tasks)
              }
            )
          }
        )
      }~
      pathPrefix("task") {
        concat(
          //#task-post-create
          pathEnd {
            concat(
              post {
                entity(as[Task]) { task =>
                  val taskCreated: Future[ActionPerformed] =
                    (taskRegistryActor ? CreateTask(task)).mapTo[ActionPerformed]
                  onSuccess(taskCreated) { performed =>
                    log.info("Created task [{}]: {}", task.title, performed.description)
                    complete((StatusCodes.Created, performed))
                  }
                }
              }
            )
          },
          path(Segment) { taskId =>
            concat(
              get {
                //#retrieve-task-byId
                val maybeTask: Future[Option[Task]] =
                  (taskRegistryActor ? GetTask(taskId.toInt)).mapTo[Option[Task]]
                rejectEmptyResponse {
                  complete(maybeTask)
                }
              },
              delete {
                //#task-delete-logic
                val taskDeleted: Future[ActionPerformed] =
                  (taskRegistryActor ? DeleteTask(taskId.toInt)).mapTo[ActionPerformed]
                onSuccess(taskDeleted) { performed =>
                  log.info("Deleted task [{}]: {}", taskId, performed.description)
                  complete((StatusCodes.OK, performed))
                }
              }
            )
          }
        )
      }
    }
  //#all-routes
}
