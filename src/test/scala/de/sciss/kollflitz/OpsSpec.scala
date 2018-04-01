package de.sciss.kollflitz

import org.scalatest.{Matchers, FlatSpec}
import Ops._

class OpsSpec extends FlatSpec with Matchers {
  val blah = "allow the invocation of extended methods"

  "Some iterable collections" should blah in {
    val in1 = List(13, 5, 8, 21, 3, 8)
    val in2 = "hello world"

    // ---- counted ----

    in1.counted shouldEqual Map(3 -> 1, 5 -> 1, 8 -> 2, 13 -> 1, 21 -> 1)
    // ok, we don't allow yet the usage of strings directly
    in2.toSeq.counted shouldEqual Map(' '->1, 'd'->1, 'e'->1, 'h'->1, 'l'->3, 'o'->2, 'r'->1, 'w'->1)

    in1.counted(99) shouldBe 0  // contract says there is a default value

    // ---- meanVariance ----

    val (m1, v1) = in1.map(_.toDouble).meanVariance
    m1 shouldBe (58.0/6 +- 0.01)
    v1 shouldBe (211.33 +- 0.01)

    // ---- allDistinct ----

    in1         .allDistinct shouldBe false
    in1.distinct.allDistinct shouldBe true
    List()      .allDistinct shouldBe true

    // ---- minIndex, maxIndex, minIndexBy, maxIndexBy ----
    in1.minIndex shouldBe 4
    in1.maxIndex shouldBe 3
    in1.minIndexBy(-_) shouldBe 3
    in1.maxIndexBy(-_) shouldBe 4

    List().minIndex shouldBe -1
    List().maxIndex shouldBe -1
  }

  "Some sequential collections" should blah in {
    val in1 = List(13, 5, 8, 21, 3, 8)
    val in2 = "hello world"

    // ---- groupWith ----

    in1.groupWith(_ > _).toVector shouldEqual Vector(List(13, 5), List(8), List(21, 3), List(8))
    in2.toSeq.groupWith(_ != _).map(_.mkString).toList shouldEqual List("hel", "lo world")

    // ---- sorting ----

    in1.sortedT         .isSorted       shouldBe true
    in1.sortByT(-_)     .isSorted       shouldBe false
    in1.sortByT(-_)     .isSortedBy(-_) shouldBe true
    in1.sortWithT(_ < _).isSorted       shouldBe true
    in1.sortWithT(_ > _).isSortedBy(-_) shouldBe true

    // ---- pairMap ----

    in1.mapPairs(_ min _) shouldEqual List(5, 5, 8, 3, 3)
    in1.mapPairs(_ -   _) shouldEqual List(8, -3, -13, 18, -5)

    // ---- differentiate ----

    in1.differentiate shouldEqual List(-8, 3, 13, -18, 5)

    // ---- integrate ----

    val in3 = List(13, -8, 3, 13, -18, 5)

    in3.integrate shouldEqual List(13, 5, 8, 21, 3, 8)
    (in1.head :: in1.differentiate).integrate shouldEqual in1

    // ---- stutter ----

    in3.stutter(0) shouldEqual List()
    in3.stutter(1) shouldEqual in3
    in3.stutter(2) shouldEqual List(
      13, 13, -8, -8, 3, 3, 13, 13, -18, -18, 5, 5)
    List().stutter(0) shouldEqual List()
    List().stutter(1) shouldEqual List()
    List().stutter(2) shouldEqual List()

    // ---- mirror ----

    in3.mirror shouldEqual List(
      13, -8, 3, 13, -18, 5, -18, 13, 3, -8, 13)
    List(1).mirror shouldEqual List(1)
    List() .mirror shouldEqual List()

    // ---- decimate ----

    in3.decimate(0)             shouldEqual in3
    in3.decimate(1)             shouldEqual in3
    in3.decimate(2)             shouldEqual List(13, 3, -18)
    in3.decimate(2, offset = 1) shouldEqual List(-8, 13, 5)
    in3.decimate(3)             shouldEqual List(13, 13)
    in3.decimate(3, offset = 1) shouldEqual List(-8, -18)
    in3.decimate(3, offset = 2) shouldEqual List(3, 5)

    List(1).decimate(0) shouldEqual List(1)
    List() .decimate(0) shouldEqual List()
    List(1).decimate(1) shouldEqual List(1)
    List() .decimate(1) shouldEqual List()
    List(1).decimate(2) shouldEqual List(1)
    List() .decimate(2) shouldEqual List()
    List(1).decimate(3) shouldEqual List(1)
    List() .decimate(3) shouldEqual List()

    // ---- wrapAt, foldAt, clipAt ----

    in3.wrapAt( 1) shouldEqual  -8
    in3.wrapAt( 0) shouldEqual  13
    in3.wrapAt(-1) shouldEqual   5
    in3.wrapAt(-7) shouldEqual   5
    in3.wrapAt(-2) shouldEqual -18
    in3.wrapAt( 4) shouldEqual -18
    in3.wrapAt( 5) shouldEqual   5
    in3.wrapAt( 6) shouldEqual  13
    in3.wrapAt(12) shouldEqual  13
    in3.wrapAt( 7) shouldEqual  -8

    an [IndexOutOfBoundsException] should be thrownBy { List().wrapAt( 0) }
    an [IndexOutOfBoundsException] should be thrownBy { List().wrapAt( 1) }
    an [IndexOutOfBoundsException] should be thrownBy { List().wrapAt(-1) }

    in3.foldAt( 1) shouldEqual  -8
    in3.foldAt( 0) shouldEqual  13
    in3.foldAt(-1) shouldEqual  -8
    in3.foldAt(-7) shouldEqual  13
    in3.foldAt(-2) shouldEqual   3
    in3.foldAt( 4) shouldEqual -18
    in3.foldAt( 5) shouldEqual   5
    in3.foldAt( 6) shouldEqual -18
    in3.foldAt(12) shouldEqual   3
    in3.foldAt( 7) shouldEqual  13

    an [IndexOutOfBoundsException] should be thrownBy { List().foldAt( 0) }
    an [IndexOutOfBoundsException] should be thrownBy { List().foldAt( 1) }
    an [IndexOutOfBoundsException] should be thrownBy { List().foldAt(-1) }

    in3.clipAt( 1) shouldEqual  -8
    in3.clipAt( 0) shouldEqual  13
    in3.clipAt(-1) shouldEqual  13
    in3.clipAt(-7) shouldEqual  13
    in3.clipAt(-2) shouldEqual  13
    in3.clipAt( 4) shouldEqual -18
    in3.clipAt( 5) shouldEqual   5
    in3.clipAt( 6) shouldEqual   5
    in3.clipAt(12) shouldEqual   5
    in3.clipAt( 7) shouldEqual   5

    an [IndexOutOfBoundsException] should be thrownBy { List().clipAt( 0) }
    an [IndexOutOfBoundsException] should be thrownBy { List().clipAt( 1) }
    an [IndexOutOfBoundsException] should be thrownBy { List().clipAt(-1) }
  }

  "Some sorted indexed collections" should blah in {
    val in1 = Vector(13, 5, 8, 21, 3, 8)
    // 3, 5, 8, 8, 13, 21

    in1.sortedT.median shouldBe 8
    in1.sortedT.percentile(  0) shouldBe  3
    in1.sortedT.percentile(100) shouldBe 21
    in1.sortedT.percentile( 75) shouldBe 13
  }

  "Some indexed collections" should "accept RNG based extension methods" in {
    implicit val r: util.Random = new util.Random(0L)
    import RandomOps._

    val in1  = Vector(13, 5, 8, 21, 3, 8)
    val scr1 = in1.shuffle()
    assert(in1.size   === scr1.size  )
    assert(in1.sorted === scr1.sorted)

    {
      implicit val r: util.Random = new util.Random(1L)
      val scr2 = in1.shuffle()
      assert(scr1 !== scr2)
    }

    val testContains = (1 to 100).forall { _ =>
      in1.contains(in1.choose())
    }
    assert(testContains)

    // ---- Urn ----

    val urn = in1.toUrn.take(in1.size * 2).toVector
    assert(urn.size == in1.size * 2)

    urn.sorted shouldEqual (in1 ++ in1).sorted
  }
}