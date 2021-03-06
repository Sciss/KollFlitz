/*
 *  GroupWithIterator.scala
 *  (KollFlitz)
 *
 *  Copyright (c) 2013-2020 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.kollflitz.impl

import scala.collection.generic.CanBuildFrom
import scala.annotation.tailrec

final class GroupWithIterator[A, From, To](peer: Iterator[A], p: (A, A) => Boolean)
                                          (implicit cbf: CanBuildFrom[From, A, To])
  extends Iterator[To] {

  private var consumed  = true
  private var e         = null.asInstanceOf[A]

  def hasNext: Boolean = !consumed || peer.hasNext

  private def pop(): A = {
    if (!consumed) return e
    if (!peer.hasNext) throw new NoSuchElementException("next on empty iterator")
    val res   = peer.next()
    e         = res
    consumed  = false
    res
  }

  def next(): To = {
    val b = cbf()

    @tailrec def loop(pr: A): Unit = {
      b += pr
      consumed = true
      if (peer.hasNext) {
        val su = pop()
        if (p(pr, su)) loop(su)
      }
    }

    loop(pop())
    b.result()
  }
}
