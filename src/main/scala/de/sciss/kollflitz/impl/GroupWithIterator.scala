package de.sciss.kollflitz.impl

import scala.collection.generic.CanBuildFrom
import scala.annotation.tailrec
import language.higherKinds

final class GroupWithIterator[A, From, To](peer: Iterator[A], p: (A, A) => Boolean)
                                          (implicit cbf: CanBuildFrom[From, A, To])
  extends Iterator[To] {

  private var consumed  = true
  private var elem      = null.asInstanceOf[A]

  def hasNext: Boolean = !consumed || peer.hasNext

  private def pop(): A = {
    if (!consumed) return elem
    if (!peer.hasNext) throw new NoSuchElementException("next on empty iterator")
    val res   = peer.next()
    elem      = res
    consumed  = false
    res
  }

  def next(): To = {
    val b = cbf()

    @tailrec def loop(pred: A): Unit = {
      b       += pred
      consumed = true
      if (peer.hasNext) {
        val succ = pop()
        if (p(pred, succ)) loop(succ)
      }
    }

    loop(pop())
    b.result()
  }
}
