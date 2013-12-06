package de.sciss.kollflitz

import org.scalatest.{Matchers, FlatSpec}

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

    in1.pairMap(_ min _) shouldEqual List(5, 5, 8, 3, 3)
    in1.pairMap(_ -   _) shouldEqual List(8, -3, -13, 18, -5)

    // ---- differentiate ----

    in1.differentiate shouldEqual List(-8, 3, 13, -18, 5)

    // ---- integrate ----

    List(13, -8, 3, 13, -18, 5).integrate shouldEqual List(13, 5, 8, 21, 3, 8)
    (in1.head :: in1.differentiate).integrate shouldEqual in1
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
    implicit val r = new util.Random(0L)
    import RandomOps._

    val in1  = Vector(13, 5, 8, 21, 3, 8)
    val scr1 = in1.scramble()
    assert(in1.size   === scr1.size  )
    assert(in1.sorted === scr1.sorted)

    {
      implicit val r = new util.Random(1L)
      val scr2 = in1.scramble()
      assert(scr1 !== scr2)
    }

    val testContains = (1 to 100).forall { _ =>
      in1.contains(in1.choose())
    }
    assert(testContains)
  }
}