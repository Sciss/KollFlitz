/*
 *  RandomOps.scala
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

package de.sciss.kollflitz

import scala.util.Random
import scala.collection.generic.CanBuildFrom
import language.higherKinds
import de.sciss.kollflitz.impl.Urn

object RandomOps {
  implicit final class KollFlitzRandomIndexedSeq[A, CC[~] <: IndexedSeq[~]](val self: CC[A]) extends AnyVal {
    /** Returns a randomly chosen element from the collection. */
    def choose()(implicit random: Random): A = self(random.nextInt(self.size))

    /** Alias for `shuffle`. */
    @deprecated("Use 'shuffle' instead", since = "0.2.2")
    def scramble[To]()(implicit random: Random, cbf: CanBuildFrom[CC[A], A, To]): To = shuffle[To]()

      /** Returns a new collection with the same contents as the input collection, but in random order. */
    def shuffle[To]()(implicit random: Random, cbf: CanBuildFrom[CC[A], A, To]): To = {
      val b     = cbf(self)
      var rem   = self: IndexedSeq[A]

      while (rem.nonEmpty) {
        val idx = random.nextInt(rem.size)
        val e   = rem(idx)
        rem     = rem.patch(idx, Nil, 1)
        b      += e
      }
      b.result()
    }

    def toUrn(implicit random: Random): Iterator[A] = toUrn(infinite = true)

    def toUrn(infinite: Boolean)(implicit random: Random): Iterator[A] = {
      // ensure immutability of input collection
      val im = self.toIndexedSeq
      new Urn(im, infinite = infinite)
    }
  }
}
