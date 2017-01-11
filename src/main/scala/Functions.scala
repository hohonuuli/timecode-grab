import org.apache.commons.math3.analysis.interpolation.LinearInterpolator
import org.mbari.math.{DoubleMath, Statlib, Matlib => JMatlib}
import scilube.Matlib._

/**
  * @author Brian Schlining
  * @since 2017-01-11T09:17:00
  */
object Functions {

  def extrap1(x: Array[Double], y: Array[Double], xi: Array[Double]): Array[Double] = {

    val fn = new LinearInterpolator().interpolate(x, y)
    val splines = fn.getPolynomials
    val pf0 = splines.head
    val pfn = splines.last


    val knots = fn.getKnots
    val k0 = knots.head
    val ko = knots(knots.length - 2)
    val kn = knots.last
    xi.map(a => {
      if (a > kn) pfn.value(a - ko)
      else if (a < k0) pf0.value(a - k0)
      else fn.value(a)
    })

  }

}
