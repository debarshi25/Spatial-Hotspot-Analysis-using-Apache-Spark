package cse512

object HotzoneUtils {

  def ST_Contains(queryRectangle: String, pointString: String ): Boolean = {
    // YOU NEED TO CHANGE THIS PART
    var rectangle = new Array[String](4)
    rectangle = queryRectangle.split(",")
    val x1 = rectangle(0).trim.toDouble
    val y1 = rectangle(1).trim.toDouble
    val x2 = rectangle(2).trim.toDouble
    val y2 = rectangle(3).trim.toDouble

    var point = new Array[String](2)
    point = pointString.split(",")
    val x = point(0).trim.toDouble
    val y = point(1).trim.toDouble

    val x_low = math.min(x1, x2)
    val x_high = math.max(x1, x2)
    val y_low = math.min(y1, y2)
    val y_high = math.max(y1, y2)

    if (x < x_low || x > x_high || y < y_low || y > y_high)
      return false
    else
      return true

  }

}
