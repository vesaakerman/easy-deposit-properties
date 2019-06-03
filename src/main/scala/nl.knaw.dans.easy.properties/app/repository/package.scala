package nl.knaw.dans.easy.properties.app

package object repository {

  implicit class MaxByOption[A](val t: TraversableOnce[A]) extends AnyVal {
    def maxByOption[B](f: A => B)(implicit cmp: Ordering[B]): Option[A] = {
      if (t.isEmpty) Option.empty
      else Option(t.maxBy(f))
    }
  }
}
