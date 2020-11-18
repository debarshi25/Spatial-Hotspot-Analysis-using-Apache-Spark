package cse512

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions._

object HotcellAnalysis {
  Logger.getLogger("org.spark_project").setLevel(Level.WARN)
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("com").setLevel(Level.WARN)

  def runHotcellAnalysis(spark: SparkSession, pointPath: String): DataFrame =
  {
    // Load the original data from a data source
    var pickupInfo = spark.read.format("com.databricks.spark.csv").option("delimiter",";").option("header","false").load(pointPath);
    pickupInfo.createOrReplaceTempView("nyctaxitrips")
    pickupInfo.show()

    // Assign cell coordinates based on pickup points
    spark.udf.register("CalculateX",(pickupPoint: String)=>((
      HotcellUtils.CalculateCoordinate(pickupPoint, 0)
      )))
    spark.udf.register("CalculateY",(pickupPoint: String)=>((
      HotcellUtils.CalculateCoordinate(pickupPoint, 1)
      )))
    spark.udf.register("CalculateZ",(pickupTime: String)=>((
      HotcellUtils.CalculateCoordinate(pickupTime, 2)
      )))
    pickupInfo = spark.sql("select CalculateX(nyctaxitrips._c5),CalculateY(nyctaxitrips._c5), CalculateZ(nyctaxitrips._c1) from nyctaxitrips")
    var newCoordinateName = Seq("x", "y", "z")
    pickupInfo = pickupInfo.toDF(newCoordinateName:_*)
    pickupInfo.createOrReplaceTempView("pickupInfo")
    pickupInfo.show()

    // Define the min and max of x, y, z
    val minX = -74.50/HotcellUtils.coordinateStep
    val maxX = -73.70/HotcellUtils.coordinateStep
    val minY = 40.50/HotcellUtils.coordinateStep
    val maxY = 40.90/HotcellUtils.coordinateStep
    val minZ = 1
    val maxZ = 31
    val numCells = (maxX - minX + 1)*(maxY - minY + 1)*(maxZ - minZ + 1)

    // YOU NEED TO CHANGE THIS PART
    val givenpointsDf0 = spark.sql("select x, y, z from pickupInfo where x >= " + minX + " and x <= " + maxX + " and y >= " + minY + " and y <= " + maxY + " and z >= " + minZ + " and z <= " + maxZ + " order by z, y, x").persist()
    givenpointsDf0.createOrReplaceTempView("Df0")

    val givenpointsDf1 = spark.sql("select x, y, z, count(*) as pointVal from Df0 group by z, y, x order by z, y, x").persist()
    givenpointsDf1.createOrReplaceTempView("Df1")

    spark.udf.register("squared", (inputX: Int) => ((HotcellUtils.squaredValue(inputX))))
    val sumofPoints = spark.sql("select count(*) as countVal, sum(pointVal) as sumVal, sum(squared(pointVal)) as squaredSum from Df1")
    sumofPoints.createOrReplaceTempView("sumofPoints")

    val pointsSum0 = sumofPoints.first().getLong(1)
    val pointsSum1 = sumofPoints.first().getDouble(2)
    val mean = (pointsSum0.toDouble / numCells.toDouble).toDouble
    val std = math.sqrt((pointsSum1.toDouble / numCells.toDouble) - (mean.toDouble * mean.toDouble)).toDouble

    spark.udf.register("NeighbourCount", (minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, inputX: Int, inputY: Int, inputZ: Int) => ((HotcellUtils.countNeighbours(minX, minY, minZ, maxX, maxY, maxZ, inputX, inputY, inputZ))))
    val neighbours = spark.sql("select NeighbourCount(" + minX + "," + minY + "," + minZ + "," + maxX + "," + maxY + "," + maxZ + "," + " a1.x, a1.y, a1.z) as nCount, count(*) as countAll, a1.x as x, a1.y as y, a1.z as z, sum(a2.pointVal) as sumTotal from Df1 as a1, Df1 as a2 where (a2.x = a1.x+1 or a2.x = a1.x or a2.x = a1.x-1) and (a2.y = a1.y+1 or a2.y = a1.y or a2.y = a1.y-1) and (a2.z = a1.z+1 or a2.z = a1.z or a2.z = a1.z-1) group by a1.z, a1.y, a1.x order by a1.z, a1.y, a1.x").persist()
    neighbours.createOrReplaceTempView("Df2");

    spark.udf.register("GScore", (x: Int, y: Int, z: Int, mean:Double, std: Double, countN: Int, sumN: Int, numCells: Int) => ((HotcellUtils.getStatistic(x, y, z, mean, std, countN, sumN, numCells))))
    val neighbours1 = spark.sql("select GScore(x, y, z, " + mean + ", " + std + ", nCount, sumTotal," +numCells+ ") as gtstat, x, y, z from Df2 order by gtstat desc")
    neighbours1.createOrReplaceTempView("Df3")

    val finalResult = spark.sql("select x, y, z from Df3")
    finalResult.createOrReplaceTempView("Df4")
    return finalResult

  }

}
