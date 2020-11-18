# Spatial Hotspot Analysis using Apache Spark

### Hot zone analysis
This performs a range join operation on a rectangle dataset and a point dataset. For each rectangle, the number of points located within the rectangle is obtained. The hotter rectangle means that it includes more points. So this is to calculate the hotness of all the rectangles. 

### Hot cell analysis

This focuses on applying spatial statistics to spatio-temporal big data in order to identify statistically significant spatial hot spots using Apache Spark. The topic is from ACM SIGSPATIAL GISCUP 2016.

The Problem Definition page is here: [http://sigspatial2016.sigspatial.org/giscup2016/problem](http://sigspatial2016.sigspatial.org/giscup2016/problem) 

The Submit Format page is here: [http://sigspatial2016.sigspatial.org/giscup2016/submit](http://sigspatial2016.sigspatial.org/giscup2016/submit)

To reduce the computation power needed，I made the following changes:

1. The input is a monthly taxi trip dataset from 2009 - 2012. For example, "yellow\_tripdata\_2009-01\_point.csv", "yellow\_tripdata\_2010-02\_point.csv".
2. Each cell unit size is 0.01 * 0.01 in terms of latitude and longitude degrees.
3. Used 1 day as the Time Step size. The first day of a month is step 1. Every month has 31 days.
4. Only considered Pick-up Location.

### Input data format

1. Point data: the input point dataset is the pickup point of New York Taxi trip datasets. Find the data from our asu google drive shared folder: [https://drive.google.com/open?id=1bN-U4nknvN5p7jiVHO-wduM7oXR5CBji](https://drive.google.com/open?id=1bN-U4nknvN5p7jiVHO-wduM7oXR5CBji)

2. Zone data (only for hot zone analysis): at "src/resources/zone-hotzone".

#### Hot zone analysis
The input point data can be any small subset of NYC taxi dataset.

#### Hot cell analysis
The input point data is a monthly NYC taxi trip dataset (2009-2012) like "yellow\_tripdata\_2009-01\_point.csv"

### Output data format

#### Hot zone analysis
All zones with their count, sorted by "rectangle" string in an ascending order.

```
"-73.795658,40.743334,-73.753772,40.779114",1
"-73.797297,40.738291,-73.775740,40.770411",1
"-73.832707,40.620010,-73.746541,40.665414",20
```

#### Hot cell analysis
The coordinates of top 50 hotest cells sorted by their G score in a descending order.

```
-7399,4075,15
-7399,4075,29
-7399,4075,22
```

### Example answers
An example input and answer are put in "testcase" folder.

### How to submit the code to Spark

1. Go to project root folder
2. Run ```sbt clean assembly```. (May need to install sbt in order to run this command).
3. Find the packaged jar in "./target/scala-2.11/CSE512-Project-Hotspot-Analysis-Template-assembly-0.1.0.jar"
4. Submit the jar to Spark using Spark command "./bin/spark-submit". A pseudo code example: ```./bin/spark-submit ~/GitHub/CSE512-Project-Hotspot-Analysis-Template/target/scala-2.11/CSE512-Project-Hotspot-Analysis-Template-assembly-0.1.0.jar test/output hotzoneanalysis src/resources/point-hotzone.csv src/resources/zone-hotzone.csv hotcellanalysis src/resources/yellow_tripdata_2009-01_point.csv```
