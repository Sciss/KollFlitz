# KollFlitz

[![Flattr this](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=sciss&url=https%3A%2F%2Fgithub.com%2FSciss%2FKollFlitz&title=KollFlitz%20Library&language=Scala&tags=github&category=software)
[![Build Status](https://travis-ci.org/Sciss/KollFlitz.svg?branch=master)](https://travis-ci.org/Sciss/KollFlitz)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.sciss/kollflitz_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.sciss/kollflitz_2.11)

## statement

KollFlitz provides extension methods for the Scala standard collection library. Things that I tend to require often,
like calculating mean, variance or percentiles, mapping groups, counting occurrences etc.

It is released under the [GNU Lesser General Public License](https://raw.github.com/Sciss/KollFlitz/master/LICENSE) v2.1+
and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`.

## note

This is used for scientific projects and rapid prototyping. The emphasis is _not_ on performance optimisation.

## requirements / installation

This project currently compiles against Scala 2.11, 2.10 using sbt 0.13.

To use the library in your project:

    "de.sciss" %% "kollflitz" % v

The current version `v` is `"0.2.0"`

## contributing

Please see the file [CONTRIBUTING.md](CONTRIBUTING.md)

## operations

### On any type of collection, sequential or not (`Iterable`)

- `counted` creates a map from elements to the frequencies of their occurrence. Example:
`List(13, 5, 8, 21, 3, 8).counted == Map(3 -> 1, 5 -> 1, 8 -> 2, 13 -> 1, 21 -> 1)`

- `mean`, `variance`, `meanVariance`. `meanVariance` returns a tuple of mean and variance, using an implicit `Fractional` type class. Example:
`List(13.0, 5.0, 9.0).meanVariance == (9.0, 32.0)`

- `normalized` transforms the elements by dividing them by the maximum absolute value. Example: `List(13.0, 5.0, -9.0).normalized == List(1.0, 0.3846, -0.6923)`

- `toMultiMap` takes a key and a value view function and produces a multi-map. Example: `List("a1", "b1", "a2", "b3", "c2", "c3").toMultiMap(_.head)(_.drop(1).toInt) == Map(b -> Vector(1, 3), a -> Vector(1, 2), c -> Vector(2, 3))`

### On sequential collections (`SeqLike`)

- `groupWith` groups the sequence such that within the adjacent elements of each group a predicate holds. Example:
`List(13, 5, 8, 21, 3, 8).groupWith(_ > _).toVector == Vector(List(13, 5), List(8), List(21, 3), List(8))`

- `sortedT`, `sortByT`, `sortedWithT` are just forwarders, but they tag the result with type `Sorted` to prevent
mistakes when calling methods such as `percentile` which assume that the collection is already sorted.

- `isSortedBy` and `isSorted` are boolean tests

- `mapPairs` is a mapping operation taking a function with two arguments which is applied with adjacent elements. Example:
`List(13, 5, 8, 21, 3, 8).mapPairs(_ - _) == List(8, -3, -13, 18, -5)`

- `foreachPairs` iterates using a function with two arguments which is applied with adjacent elements. `foreachPairs` is to `mapPairs` what `foreach` is to `map`.

- `differentiate` is a special pair map that uses a numeric type class to calculate the pairwise differences. Example:
`List(13, 5, 8, 21, 3, 8).differentiate == List(-8, 3, 13, -18, 5)`

- `integrate` is the opposite of `differentiate`. It aggregates a running sum. Example:
`List(13, -8, 3, 13, -18, 5).integrate == List(13, 5, 8, 21, 3, 8)`

### On sorted indexed collections (`IndexedSeq @@ Sorted`)

- `percentile` returns a given percentile of the sorted collection. It picks an integer index and does not interpolate
values. Example: `Vector(13, 5, 8, 21, 3, 8).sortedT.percentile(75) == 13`

- `median` is a shortcut for `percentile(50)`

### Random operations

Random operation require an implicit value of type `util.Random`. They must be imported via `RandomOps._`.

- `choose` returns a random element of an indexed sequence.
- `scramble` returns a copy of the collection where the order of elements is randomised.
- `toUrn` returns a scrambled iterator (an argument specifies whether it is infinite or not)

## limitations

The acrobatics for allowing `String` and `Array` to be extended by these operations have not been performed.
