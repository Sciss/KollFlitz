# KollFlitz

## statement

KollFlitz provides extension methods for the Scala standard collection library. Things that I tend to require often,
like calculating mean, variance or percentiles, mapping groups, counting occurrences etc.

It is released under the [GNU Lesser General Public License](https://raw.github.com/Sciss/KollFlitz/master/LICENSE) v2.1+
and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`.

## note

This is used for scientific projects and rapid prototyping. The emphasis is _not_ on performance optimisation.

## requirements / installation

This project currently compiles against Scala 2.10 using sbt 0.13.

To use the library in your project:

    "de.sciss" %% "kollflitz" % v

The current version `v` is `"0.1.+"`

## operations

### On any type of collection, sequential or not (`Iterable`)

- `counted` creates a map from elements to the frequencies of their occurrence. Example:
`List(13, 5, 8, 21, 3, 8).counted == Map(3 -> 1, 5 -> 1, 8 -> 2, 13 -> 1, 21 -> 1)`

- `meanVariance` returns a tuple of mean and variance, using an implicit `Fractional` type class. Example:
`List(13.0, 5.0, 9.0).meanVariance == (9.0, 32.0)`

### On sequential collections (`SeqLike`)

- `groupWith` groups the sequence such that within the adjacent elements of each group a predicate holds. Example:
`List(13, 5, 8, 21, 3, 8).groupWith(_ > _).toVector == Vector(List(13, 5), List(8), List(21, 3), List(8))`

- `sortedT`, `sortByT`, `sortedWithT` are just forwarders, but they tag the result with type `Sorted` to prevent
mistakes when calling methods such as `percentile` which assume that the collection is already sorted.

- `isSortedBy` and `isSorted` are boolean tests

- `pairMap` is a mapping operation taking a function with two arguments which is applied with adjacent elements. Example:
`List(13, 5, 8, 21, 3, 8).pairMap(_ - _) == List(8, -3, -13, 18, -5)`

- `differentiate` is a special pair map that uses a numeric type class to calculate the pairwise differences. Example:
`List(13, 5, 8, 21, 3, 8).differentiate == List(-8, 3, 13, -18, 5)`

- `integrate` is the opposite of `differentiate`. It aggregates a running sum. Example:
`List(0, -8, 3, 13, -18, 5).integrate == List(13, 5, 8, 21, 3, 8)`

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