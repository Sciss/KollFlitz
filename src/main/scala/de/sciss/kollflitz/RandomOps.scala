package de.sciss.kollflitz

import scala.util.Random
import scala.collection.generic.CanBuildFrom
import language.higherKinds
import de.sciss.kollflitz.impl.Urn

object RandomOps {
  implicit final class KollFlitzRandomIndexedSeq[A, CC[~] <: IndexedSeq[~]](val self: CC[A]) extends AnyVal {
    /** Returns a randomly chosen element from the collection. */
    def choose()(implicit random: Random): A = self(random.nextInt(self.size))

    /** Returns a new collection with the same contents as the input collection, but in random order. */
    def scramble[To]()(implicit random: Random, cbf: CanBuildFrom[CC[A], A, To]): To = {
      val b     = cbf(self)
      var rem   = self: IndexedSeq[A]

      while (rem.nonEmpty) {
        val idx   = random.nextInt(rem.size)
        val elem  = rem(idx)
        rem       = rem.patch(idx, Nil, 1)
        b        += elem
      }
      b.result()
    }

    def toUrn(implicit random: Random): Iterator[A] = toUrn(infinite = true)

    def toUrn(infinite: Boolean)(implicit random: Random): Iterator[A] = {
      // XXX TODO: this is a bit dirty...
      val im = if (self.isInstanceOf[Immutable]) self.asInstanceOf[Vec[A]] else self.toVector
      new Urn(im, infinite = infinite)
    }
  }
}
