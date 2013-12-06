package de.sciss.kollflitz
package impl

import scala.util.Random

final class Urn[A](elements0: Vec[A], infinite: Boolean)(implicit r: Random)
  extends Iterator[A] {

  private var bag = elements0

  def hasNext: Boolean = bag.nonEmpty || (infinite && elements0.nonEmpty)

  def next(): A = {
    if (bag.isEmpty && infinite) bag = elements0
    val idx   = r.nextInt(bag.size)
    val elem  = bag(idx)
    bag       = bag.patch(idx, Nil, 1)
    elem
  }
}