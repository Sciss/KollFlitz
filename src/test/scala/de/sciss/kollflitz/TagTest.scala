package de.sciss.kollflitz

import Ops._

object TagTest extends App {
  val vec     = Vec(3, 5, 1, 10, -1, 22)
  val tagged  = tag[Sorted](vec) : Vec[Int] @@ Sorted

  //  tagged: Tagged[Sorted] // ok
  //
  //  implicitly[Vec[Int] @@ Sorted <:< Tagged[Sorted]]
  //
  //  class foo[CC <: IndexedSeq[_]](cc: CC) {
  //    // def bar(implicit ev: CC <:< Tagged[Sorted]) = ()
  //  }
  //
  //  new foo(tagged) // .bar

  val median  = tagged.percentile(50)
  val q25     = vec.sortedT.percentile(25)
  println(s"Median of $vec is $median")
}
