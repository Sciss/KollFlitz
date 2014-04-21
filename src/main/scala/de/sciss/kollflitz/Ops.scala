/*
 *  Ops.scala
 *  (KollFlitz)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.kollflitz

import scala.collection.mutable
import scala.collection.SeqLike
import scala.collection.generic.CanBuildFrom
import de.sciss.kollflitz.impl.GroupWithIterator
import language.higherKinds

object Ops {
  /** Enrichment methods for any type of collection, sequential or not. */
  implicit final class KollFlitzIterable[A, CC[~] <: Iterable[~]](val self: CC[A]) extends AnyVal {
    /** Produces a map from the input elements to the frequency in which they appear in the input collection.
      *
      * For example:
      * {{
      *   val x = List("a", "a", "b", "a")
      *   val m = x.counted
      * }}
      *
      * produces `Map("a" -> 3, "b" -> 1)`. The map has a default value of zero,
      * so calling `m("c")` returns zero.
      *
      * @return a map with the elements counted.
      */
    def counted: Map[A, Int] = {
      var m   = Map.empty[A, Int].withDefaultValue(0)
      val it  = self.iterator
      while (it.hasNext) {
        val e = it.next()
        m     = m.updated(e, m(e) + 1)
      }
      m
    }

    def mean(implicit num: Fractional[A]): A = {
      import num._
      var sum   = zero
      var size  = zero
      val it    = self.iterator
      while (it.hasNext) {
        val e = it.next()
        sum   = plus(sum , e  )
        size  = plus(size, one)
      }
      div(sum, size)
    }

    def variance(implicit num: Fractional[A]): A = varianceImpl(mean)

    private def varianceImpl(mean: A)(implicit num: Fractional[A]): A = {
      import num._
      var variance  = zero
      val it        = self.iterator
      while (it.hasNext) {
        val e     = it.next()
        val d     = minus(e, mean)
        variance  = plus(variance, times(d, d))
      }
      variance
    }

    /** Calculates the mean and variance of the collection.
      *
      * @param num  numerical view of the element type
      * @return a tuple consisting of `_1` mean and `_2` variance.
      */
    def meanVariance(implicit num: Fractional[A]): (A, A) = {
      val m = mean
      val v = varianceImpl(m)
      (m, v)
    }

    /** Normalizes the elements by finding the maximum absolute value
      * and dividing each element by this value.
      *
      * If the collection is empty or the maximum absolute value is zero,
      * the original collection is returned.
      *
      * @param num  numerical view of the element type
      */
    def normalized(implicit num: Fractional[A], cbf: CanBuildFrom[CC[A], A, CC[A]]): CC[A] = {
      if (self.isEmpty) return self
      val mx = self.maxBy(num.abs)
      if (mx == num.zero) return self

      val b = cbf(self)
      val it    = self.iterator
      while (it.hasNext) {
        val e   = it.next()
        val e1  = num.div(e, mx)
        b += e1
      }
      b.result()
    }

    def toMultiMap[K, V, Values](key: A => K)(value: A => V)
                                (implicit cbfv: CanBuildFrom[Nothing, V, Values]): Map[K, Values] = {
      val b = mutable.Map.empty[K, mutable.Builder[V, Values]]
      self.foreach { elem =>
        b.getOrElseUpdate(key(elem), cbfv()) += value(elem)
      }
      b.map { case (k, vb) => (k, vb.result()) } (collection.breakOut)
    }
  }

  /** Enrichment methods for sequential collections. */
  implicit final class KollFlitzSeqLike[A, Repr](val self: SeqLike[A, Repr] with Repr) extends AnyVal {
    def sortByT[B](f: A => B)(implicit ord: Ordering[B]): Repr @@ Sorted = tag[Sorted](self.sortBy(f))

    def sortWithT(lt: (A, A) => Boolean): Repr @@ Sorted = tag[Sorted](self.sortWith(lt))

    def sortedT[B >: A](implicit ord: Ordering[B]): Repr @@ Sorted = tag[Sorted](self.sorted(ord))

    def isSortedBy[B](fun: A => B)(implicit ord: Ordering[B]): Boolean = {
      val it = self.iterator
      if (!it.hasNext) return true
      var pr = it.next()
      while (it.hasNext) {
        val su = it.next()
        if (ord.gt(fun(pr), fun(su))) return false
        pr = su
      }
      true
    }

    def isSorted[B >: A](implicit ord: Ordering[B]): Boolean = isSortedBy[B](identity)

    /** Clumps the collection into groups based on a predicate which determines if successive elements
      * belong to the same group.
      *
      * For example:
      * {{
      *   val x = List("a", "a", "b", "a", "b", "b")
      *   x.groupWith(_ == _).to[Vector]
      * }}
      *
      * produces `Vector(List("a", "a"), List("b"), List("a"), List("b", "b"))`.
      *
      * @param p    a function which is evaluated with successive pairs of the input collection. As long
      *             as the predicate holds (the function returns `true`), elements are lumped together.
      *             When the predicate becomes `false`, a new group is started.
      *
      * @param cbf  a builder factory for the group type
      * @tparam To  the group type
      * @return     an iterator over the groups.
      */
    def groupWith[To](p: (A, A) => Boolean)(implicit cbf: CanBuildFrom[Repr, A, To]): Iterator[To] =
      new GroupWithIterator(self.iterator, p)

    def mapPairs[B, To](fun: (A, A) => B)(implicit cbf: CanBuildFrom[Repr, B, To]): To = {
      val b   = cbf(self)
      val it  = self.iterator
      if (it.hasNext) {
        var pr = it.next()
        while (it.hasNext) {
          val su = it.next()
          b     += fun(pr, su)
          pr     = su
        }
      }
      b.result()
    }

    def foreachPair(fun: (A, A) => Unit): Unit = {
      val it  = self.iterator
      if (it.hasNext) {
        var pr = it.next()
        while (it.hasNext) {
          val su = it.next()
          fun(pr, su)
          pr     = su
        }
      }
    }

    /** Differentiates the collection by calculating the pairwise difference of the elements.
      *
      * @param num    the numerical view of the elements
      * @param cbf    the result type builder factory
      * @tparam To    the result collection type
      * @return a new collection having a size one less than the input collection. the first element will
      *         be the different of the second and first element of the input sequence, etc.
      */
    def differentiate[To](implicit num: Numeric[A], cbf: CanBuildFrom[Repr, A, To]): To =
      mapPairs { (a, b) => num.minus(b, a) }

    /** Integrates the collection by aggregating the elements step by step.
      *
      * @param num    the numerical view of the elements
      * @param cbf    the result type builder factory
      * @tparam To    the result collection type
      * @return a new collection having the same size as the input collection. the first element will
      *         be identical to the first element in the input sequence, the second element will be
      *         the sum of the first and second element of the input sequence, etc.
      */
    def integrate[To](implicit num: Numeric[A], cbf: CanBuildFrom[Repr, A, To]): To = {
      import num._
      val b     = cbf(self)
      val it    = self.iterator
      var agg   = zero
      while (it.hasNext) {
        val e = it.next()
        agg   = plus(agg, e)
        b    += agg
      }
      b.result()
    }
  }

  /** Enrichment methods for random access collections. */
  implicit final class KollFlitzSortedIndexedSeq[A](val self: IndexedSeq[A] @@ Sorted) extends AnyVal {
    /** Nearest percentile (rounded index, no interpolation). */
    def percentile(n: Int): A = self((self.size * n - 50) / 100)

    /** Median found by rounding the index (no interpolation). */
    def median: A = percentile(50)
  }
}
