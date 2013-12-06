package de.sciss

import scala.collection.generic.CanBuildFrom
import de.sciss.kollflitz.impl.GroupWithIterator
import language.higherKinds
import scala.collection.SeqLike

package object kollflitz {
  /** `Vec` is an alias for `immutable.IndexedSeq`. You will thus get `Vector` instances
    * while letting Scala still pick the default implementation and maintaining an opaque type.
    */
  val  Vec      = collection.immutable.IndexedSeq
  type Vec[+A]  = collection.immutable.IndexedSeq[A]

  // courtesy of Miles Sabin
  type Tagged[U]  = { type Tag  = U }
  type @@ [+T, U] = T with Tagged[U]

  private val anyTagger = new Tagger[Any]
  final class Tagger[U] private[kollflitz] {
    def apply[T](t : T): T @@ U = t.asInstanceOf[T @@ U]
  }
  def tag[U]: Tagger[U] = anyTagger.asInstanceOf[Tagger[U]]

  sealed trait Sorted

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
    def counted: Map[A, Int] = (Map.empty[A, Int].withDefaultValue(0) /: self)((m, e) => m.updated(e, m(e) + 1))

    def mean(implicit num: Fractional[A]): A = {
      var sum   = num.zero
      var size  = num.zero
      val one   = num.one
      import num.mkNumericOps
      self.foreach { e =>
        sum  += e
        size += one
      }
      sum / size
    }

    def variance(implicit num: Fractional[A]): A = varianceImpl(mean)

    private def varianceImpl(mean: A)(implicit num: Fractional[A]): A = {
      import num.mkNumericOps
      var variance = num.zero
      self.foreach { e =>
        val d = e - mean
        variance += d * d
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
  }

  /** Enrichment methods for sequential collections. */
  implicit final class KollFlitzSeqLike[A, Repr](val self: SeqLike[A, Repr] with Repr) extends AnyVal {
    def sortByT[B](f: A => B)(implicit ord: Ordering[B]): Repr @@ Sorted = tag[Sorted](self.sortBy(f))

    def sortWithT(lt: (A, A) => Boolean): Repr @@ Sorted = tag[Sorted](self.sortWith(lt))

    def sortedT[B >: A](implicit ord: Ordering[B]): Repr @@ Sorted = tag[Sorted](self.sorted(ord))

    def isSortedBy[B](fun: A => B)(implicit ord: Ordering[B]): Boolean = {
      val it = self.iterator
      if (!it.hasNext) return true
      var pred = it.next()
      while (it.hasNext) {
        val succ = it.next()
        if (ord.gt(fun(pred), fun(succ))) return false
        pred = succ
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

    def pairMap[B, To](fun: (A, A) => B)(implicit cbf: CanBuildFrom[Repr, B, To]): To = {
      val b     = cbf(self)
      val iter  = self.iterator
      if (iter.hasNext) {
        var pred = iter.next()
        while (iter.hasNext) {
          val succ = iter.next()
          b   += fun(pred, succ)
          pred = succ
        }
      }
      b.result()
    }

    /** Differentiates the collection by calculating the pairwise difference of the elements.
      *
      * @param num    the numerical view of the elements
      * @param cbf    the result type builder factory
      * @tparam To    the result collection type
      * @return a new collection having a size one less than the input collection. the first element will
      *         be the different of the second and first element of the input sequence, etc.
      */
    def differentiate[To](implicit num: Numeric[A], cbf: CanBuildFrom[Repr, A, To]): To = {
      import num.mkNumericOps
      pairMap {
        case (pred, succ) => succ - pred
      }
    }

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
      val b     = cbf(self)
      val iter  = self.iterator
      var agg   = num.zero
      while (iter.hasNext) {
        val elem = iter.next()
        agg = num.plus(agg, elem)
        b += agg
      }
      b.result()
    }
  }

  //  implicit final class KollFlitzSortedIndexedSeq[A](val sq: IndexedSeq[A] @@ Sorted) extends AnyVal {
  //
  //  }

  /** Enrichment methods for random access collections. */
  implicit final class KollFlitzSortedIndexedSeq[A](val self: IndexedSeq[A] @@ Sorted) extends AnyVal {
    /** Nearest percentile (rounded index, no interpolation). */
    def percentile(n: Int): A = self((self.size * n - 50) / 100)

    /** Median found by rounding the index (no interpolation). */
    def median: A = percentile(50)

    // def percentile(n: Int)(implicit sorted: CC <:< Tagged[Sorted]): A = sq((sq.size * n - 50) / 100)
  }
}
