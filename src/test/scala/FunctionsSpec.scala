import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Brian Schlining
  * @since 2017-01-11T09:39:00
  */
class FunctionsSpec extends FlatSpec with Matchers {

  "Functions" should "extrapolate" in {

    val x = Array[Double](1, 2, 3, 4, 5)
    val y = Array[Double](1, 2, 3, 4, 5)
    val xi = Array[Double](2, 4, 6, 8, 10)
    val yi = Functions.extrap1(x, y, xi)
    yi should be (Array[Double](2, 4, 6, 8, 10))

  }

  it should "extraplate with complexity" in {
    val x = Array[Double](1, 2, 3, 4, 5)
    val y = Array[Double](1, 2, 8, 4, 5)
    val xi = Array[Double](2, 4, 6, 8, 10)
    val yi = Functions.extrap1(x, y, xi)
    yi should be (Array[Double](2, 4, 6, 8, 10))
  }

  it should "extraplate with more complexity" in {
    val x = Array[Double](1, 2, 3, 4, 5)
    val y = Array[Double](1, 2, 8, 4, 8)
    val xi = Array[Double](2, 4, 6, 8, 10)
    val yi = Functions.extrap1(x, y, xi)
    yi should be (Array[Double](2, 4, 12, 20, 28))
  }


}
