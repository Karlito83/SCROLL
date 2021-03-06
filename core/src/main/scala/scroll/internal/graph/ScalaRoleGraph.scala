package scroll.internal.graph

import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.Graphs

import scala.collection.JavaConverters._

/**
  * Scala specific implementation of a [[scroll.internal.graph.RoleGraph]] using
  * a graph as underlying data model.
  *
  * @param checkForCycles set to true to forbid cyclic role playing relationships
  */
class ScalaRoleGraph(checkForCycles: Boolean = true) extends RoleGraph {

  protected val MERGE_MESSAGE: String = "You can only merge RoleGraphs of the same type!"

  private val root = GraphBuilder.directed().build[Object]()

  override def addPart(other: RoleGraph): Boolean = {
    require(other.isInstanceOf[ScalaRoleGraph], MERGE_MESSAGE)

    val target = other.asInstanceOf[ScalaRoleGraph].root
    if (!target.nodes().isEmpty) {
      target.edges().forEach(p => {
        val _ = root.putEdge(p.source(), p.target())
      })
      checkCycles()
      true
    } else {
      false
    }
  }

  override def detach(other: RoleGraph): Unit = {
    require(null != other)
    val target = other.asInstanceOf[ScalaRoleGraph].root
    target.edges().forEach(p => {
      removeBinding(p.source(), p.target())
    })
  }

  private[this] def checkCycles(): Unit = {
    if (checkForCycles && Graphs.hasCycle(root)) {
      throw new RuntimeException("Cyclic role-playing relationship found!")
    }
  }

  override def addBinding(player: AnyRef, role: AnyRef): Unit = {
    require(null != player)
    require(null != role)
    val _ = root.putEdge(player, role)
    if (checkForCycles && Graphs.hasCycle(root)) {
      throw new RuntimeException(s"Cyclic role-playing relationship for player '$player' found!")
    }
  }

  override def removeBinding(player: AnyRef, role: AnyRef): Unit = {
    val _ = root.removeEdge(player, role)
  }

  override def removePlayer(player: AnyRef): Unit = {
    val _ = root.removeNode(player)
  }

  private def filter(g: Graph[AnyRef], player: AnyRef): Seq[AnyRef] =
    if (containsPlayer(player)) {
      (Graphs.reachableNodes(g, player).asScala - player).toSeq
    } else {
      Seq.empty[AnyRef]
    }

  override def roles(player: AnyRef): Seq[AnyRef] = filter(root, player)

  override def facets(player: AnyRef): Seq[Enumeration#Value] =
    if (containsPlayer(player)) {
      root.successors(player).asScala.toSeq.collect { case v: Enumeration#Value => v }
    } else {
      Seq.empty[Enumeration#Value]
    }

  override def containsPlayer(player: AnyRef): Boolean = root.nodes().contains(player)

  override def allPlayers: Seq[AnyRef] = root.nodes().asScala.toSeq

  override def predecessors(player: AnyRef): Seq[AnyRef] = filter(Graphs.transpose(root), player)
}
