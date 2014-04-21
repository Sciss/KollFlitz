/*
 *  package.scala
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

package de.sciss

import language.higherKinds

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
}