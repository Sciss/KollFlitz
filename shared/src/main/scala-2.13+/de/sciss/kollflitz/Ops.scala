/*
 *  Ops.scala
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

import de.sciss.kollflitz.impl.GroupWithIterator

import scala.collection.{BuildFrom, Factory, SeqOps, mutable}
import scala.language.higherKinds

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

    /** Calculates the numerical mean value based on the supplied `Fractional` type class. */
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

    /** Calculates the numerical variance value based on the supplied `Fractional` type class. */
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
    def normalized(implicit num: Fractional[A], cbf: BuildFrom[CC[A], A, CC[A]]): CC[A] = {
      if (self.isEmpty) return self
      val mx = self.maxBy(num.abs)
      if (mx == num.zero) return self

      val b = cbf.newBuilder(self)
      val it    = self.iterator
      while (it.hasNext) {
        val e   = it.next()
        val e1  = num.div(e, mx)
        b += e1
      }
      b.result()
    }

    /** Produces a multi-map of this collection, mapping each element to keys and values
      * based on the supplied functions. A multi-map is a `Map` where the values are
      * collections.
      *
      * @param key        the function that calculates the key from an element.
      * @param value      the function that calculates a single value element from an element.
      * @param cbfv       the builder factory for the values collection
      * @tparam K         the key type
      * @tparam V         the value collection's element type
      * @tparam Values    the collection type of the values
      */
    def toMultiMap[K, V, Values](key: A => K)(value: A => V)
                                (implicit cbfv: Factory[V, Values]): Map[K, Values] = {
      val b = mutable.Map.empty[K, mutable.Builder[V, Values]]
      self.foreach { elem =>
        b.getOrElseUpdate(key(elem), cbfv.newBuilder) += value(elem)
      }
      b.iterator.map { case (k, vb) => (k, vb.result()) } .toMap
    }

    /** Determines whether all elements of this collection are unique, in other words,
      * whether no two equal elements exist. For an empty collection, this is `true`.
      */
    def allDistinct: Boolean = {
      val set = mutable.Set.empty[A]
      self.forall(set.add)
    }
  }

  // handles negative numbers differently than a % b
  @inline private[this] def mod(a: Int, b: Int): Int = if (b == 0) 0 else {
    // No. Fucking. Way.
    var in = a
    if (a >= b) {
      in -= b
      if (in < b) return in
    } else if (a < 0) {
      in += b
      if (in >= 0) return in
    } else return in

    val c = in % b
    if (c < 0) c + b else c
  }

  @inline private[this] def wrap(in: Int, low: Int, high: Int): Int = mod(in - low, high - low + 1) + low

  @inline private[this] def fold(in: Int, low: Int, high: Int): Int = {
    val b   = high - low
    val b2  = b + b
    val c0  = mod(in - low, b2)
    val c   = if (c0 > b) b2 - c0 else c0
    c + low
  }

  @inline private[this] def clip(in: Int, low: Int, high: Int): Int = math.max(low, math.min(high, in))

  /** Enrichment methods for sequential collections. */
  implicit final class KollFlitzSeqLike[A, CC[_], Repr](val self: SeqOps[A, CC, Repr] with Repr) extends AnyVal {
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
    def groupWith[To](p: (A, A) => Boolean)(implicit cbf: BuildFrom[Repr, A, To]): Iterator[To] =
      new GroupWithIterator(self, p)

    def mapPairs[B, To](fun: (A, A) => B)(implicit cbf: BuildFrom[Repr, B, To]): To = {
      val b   = cbf.newBuilder(self)
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
    def differentiate[To](implicit num: Numeric[A], cbf: BuildFrom[Repr, A, To]): To =
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
    def integrate[To](implicit num: Numeric[A], cbf: BuildFrom[Repr, A, To]): To = {
      import num._
      val b     = cbf.newBuilder(self)
      val it    = self.iterator
      var agg   = zero
      while (it.hasNext) {
        val e = it.next()
        agg   = plus(agg, e)
        b    += agg
      }
      b.result()
    }

    /** Creates a new collection in which each element of the input
      * collection is repeated `n` times.
      *
      * For example, `List(1, 2, 3).stutter(2)` produces `List(1, 1, 2, 2, 3, 3)`.
      *
      * @param  n     the number of times to repeat each element. If zero or less,
      *               the output collection will be empty.
      * @param cbf    the result type builder factory
      * @tparam To    the result collection type
      */
    def stutter[To](n: Int)(implicit cbf: BuildFrom[Repr, A, To]): To = {
      val b   = cbf.newBuilder(self)
      val it  = self.iterator
      while (it.hasNext) {
        val e = it.next()
        var i = 0
        while (i < n) {
          b += e
          i += 1
        }
      }
      b.result()
    }

    /** Concatenates this sequence with the tail of its reversed sequence.
      *
      * For example, `List(1, 2, 3).mirror` produces `List(1, 2, 3, 2, 1)`
      *
      * @param cbf    the result type builder factory
      * @tparam To    the result collection type
      */
    def mirror[To](implicit cbf: BuildFrom[Repr, A, To]): To = {
      val b   = cbf.newBuilder(self)
      val itF = self.iterator
      while (itF.hasNext) {
        val e = itF.next()
        b += e
      }
      val itR = self.reverseIterator
      if (itR.hasNext) {
        itR.next()
        while (itR.hasNext) {
          val e = itR.next()
          b += e
        }
      }
      b.result()
    }

    /** The opposite of `stutter` - from every subsequent `n` elements, `n - 1` will be dropped.
      *
      * For example, `(1 to 10).decimate(2)` produces `Seq(1, 3, 5, 7, 9)`.
      *
      * @param  n     the number of times to repeat each element. If one or less,
      *               the output collection will have the same elements as the input collection.
      * @param offset the number of initial elements to drop. This will be clipped to
      *               the range from zero to `n - 1`.
      * @param cbf    the result type builder factory
      * @tparam To    the result collection type
      */
    def decimate[To](n: Int, offset: Int = 0)(implicit cbf: BuildFrom[Repr, A, To]): To = {
      val b     = cbf.newBuilder(self)
      val n0    = math.max(1, n)
      val off   = math.max(0, math.min(offset, n0 - 1))
      val it    = self.iterator
      var skip  = off + 1
      while (it.hasNext) {
        val e = it.next()
        skip -= 1
        if (skip == 0) {
          b += e
          skip = n0
        }
      }
      b.result()
    }

    /** A variant of `apply` that wraps the index around `(0 until size)`.
      * Throws an `IndexOutOfBoundsException` if the collection is empty.
      *
      * For example, `List(2, 3, 4, 5).wrapAt(-1)` gives `5` and `List(2, 3, 4, 5).wrapAt(4)` gives `2`.
      */
    def wrapAt(n: Int): A = {
      val n1 = wrap(n, 0, self.size - 1)
      self(n1)
    }

    /** A variant of `apply` that folds (mirrors) the index around `(0 until size)`.
      * Throws an `IndexOutOfBoundsException` if the collection is empty.
      *
      * For example, `List(2, 3, 4, 5).foldAt(-1)` gives `3` and `List(2, 3, 4, 5).foldAt(4)` gives `4`.
      */
    def foldAt(n: Int): A = {
      val n1 = fold(n, 0, self.size - 1)
      self(n1)
    }

    /** A variant of `apply` that clips the index around `(0 until size)`.
      * Throws an `IndexOutOfBoundsException` if the collection is empty.
      *
      * For example, `List(2, 3, 4, 5).clipAt(-1)` gives `2` and `List(2, 3, 4, 5).clipAt(4)` gives `5`.
      */
    def clipAt(n: Int): A = {
      val n1 = clip(n, 0, self.size - 1)
      self(n1)
    }

    /** Returns the index of the minimum element, according to the given `Ordering`.
      * Yields `-1` if the collection is empty.
      */
    def minIndex[B >: A](implicit ord: Ordering[B]): Int = {
      var res = -1
      val it = self.iterator
      if (it.nonEmpty) {
        var em  = it.next()
        res     = 0
        var i   = 1
        it.foreach { e =>
          if (ord.lt(e, em)) {
            res = i
            em  = e
          }
          i += 1
        }
      }
      res
    }

    /** Returns the index of the minimum element, according to the given `Ordering`
      * and a function `f` that maps each element to a value for comparison.
      * Yields `-1` if the collection is empty.
      */
    def minIndexBy[B >: A](f: A => B)(implicit ord: Ordering[B]): Int = {
      var res = -1
      val it = self.iterator
      if (it.nonEmpty) {
        var em  = f(it.next())
        res     = 0
        var i   = 1
        it.foreach { e =>
          val ef = f(e)
          if (ord.lt(ef, em)) {
            res = i
            em  = ef
          }
          i += 1
        }
      }
      res
    }

    /** Returns the index of the maximum element, according to the given `Ordering`.
      * Yields `-1` if the collection is empty.
      */
    def maxIndex[B >: A](implicit ord: Ordering[B]): Int = {
      var res = -1
      val it = self.iterator
      if (it.nonEmpty) {
        var em  = it.next()
        res     = 0
        var i   = 1
        it.foreach { e =>
          if (ord.gt(e, em)) {
            res = i
            em  = e
          }
          i += 1
        }
      }
      res
    }

    /** Returns the index of the maximum element, according to the given `Ordering`
      * and a function `f` that maps each element to a value for comparison.
      * Yields `-1` if the collection is empty.
      */
    def maxIndexBy[B >: A](f: A => B)(implicit ord: Ordering[B]): Int = {
      var res = -1
      val it = self.iterator
      if (it.nonEmpty) {
        var em  = f(it.next())
        res     = 0
        var i   = 1
        it.foreach { e =>
          val ef = f(e)
          if (ord.gt(ef, em)) {
            res = i
            em  = ef
          }
          i += 1
        }
      }
      res
    }
  }

  /** Enrichment methods for random access collections. */
  implicit final class KollFlitzSortedIndexedSeq[A, CC[_], Repr](val self: SeqOps[A, CC, Repr] @@ Sorted)
    extends AnyVal {

    /** Nearest percentile (rounded index, no interpolation). */
    def percentile(n: Int): A = self((self.size * n - 50) / 100)

    /** Median found by rounding the index (no interpolation). */
    def median: A = percentile(50)
  }
}
