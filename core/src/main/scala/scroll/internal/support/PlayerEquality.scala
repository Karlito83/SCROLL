package scroll.internal.support

import scroll.internal.ICompartment

import scala.reflect.ClassTag

trait PlayerEquality {
  self: ICompartment =>

  def equalsPlayer[W <: AnyRef : ClassTag](a: IPlayer[W, _], b: IPlayer[W, _]): Boolean = {
    val coreA = coreFor(a.wrapped)
    val coreB = coreFor(b.wrapped)
    if (coreA.size == 1) {
      coreA.headOption.exists(coreB.lastOption.contains)
    } else if (coreB.size == 1) {
      coreB.headOption.exists(coreA.lastOption.contains)
    } else {
      coreA == coreB
    }
  }

  def equalsAny[W <: AnyRef : ClassTag](a: IPlayer[W, _], b: Any): Boolean = {
    val coreA = coreFor(a.wrapped)
    if (coreA.size == 1) {
      coreA.headOption.contains(b)
    } else {
      coreA.lastOption.contains(b)
    }
  }

}
