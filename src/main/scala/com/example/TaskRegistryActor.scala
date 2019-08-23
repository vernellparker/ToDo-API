package com.example

//#task-registry-actor
import akka.actor.{ Actor, ActorLogging, Props }

//#task-case-classes
final case class Task(title: String, taskId: Int, taskDescription: String)
final case class Tasks(tasks: Seq[Task])
//#task-case-classes

object TaskRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetTasks
  final case class CreateTask(task: Task)
  final case class GetTask(taskId: Int)
  final case class DeleteTask(taskId: Int)

  def props: Props = Props[TaskRegistryActor]
}

class TaskRegistryActor extends Actor with ActorLogging {
  import TaskRegistryActor._

  var tasks = Set.empty[Task]

  def receive: Receive = {
    case GetTasks =>
      sender() ! Tasks(tasks.toSeq)
    case CreateTask(task) =>
      tasks += task
      sender() ! ActionPerformed(s"Task ${task.title} created.")
    case GetTask(task) =>
      sender() ! tasks.find(_.taskId == task)
    case DeleteTask(task) =>
      tasks.find(_.taskId == task) foreach { task => tasks -= task }
      sender() ! ActionPerformed(s"Task ${task} deleted.")
  }
}
//task-registry-actor