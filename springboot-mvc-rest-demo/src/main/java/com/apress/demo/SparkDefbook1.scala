/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package com.spark.book
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{ approx_count_distinct, expr, max, min, col, collect_list, lit, map }
import java.net.URI
import org.apache.hadoop.fs.{ FileSystem, Path, FileStatus, LocatedFileStatus, RemoteIterator }
/**
 * Usage: Aggregation [partitions] [numElem] [blockSize]
 */
object Aggregation {

  def listFiles(iter: RemoteIterator[LocatedFileStatus]) = {
    def go(iter: RemoteIterator[LocatedFileStatus], acc: List[URI]): List[URI] = {
      if (iter.hasNext) {
        val uri = iter.next.getPath.toUri
        go(iter, uri :: acc)
      } else {
        acc
      }
    }
    go(iter, List.empty[java.net.URI])
  }

  def latest = (fapi: DataFrame, u: String, pkey: String, timestamp: String) => {

    val f = List(timestamp)
    val aggrExprs = f.map(x => max(x).alias(x))
    val dedupe = fapi.select(u, timestamp, pkey)
      .groupBy(u).agg(aggrExprs.head, aggrExprs.tail: _*)
      // .drop(pkey)
      .withColumnRenamed(u, "j" + u)
      .withColumnRenamed(timestamp, "j" + timestamp)
    dedupe.filter(dedupe("jCustomerID") === "12493.0").show(100, false)
    dedupe
    // .withColumn("latest", col("FIRST"))
    fapi.join(dedupe, fapi(u) === dedupe("j" + u), "left_outer")
      //  .withColumn("latest", col("FIRST"))
      .drop("j" + u)

  }

  def readfile(path: Option[String], spark: SparkSession): Option[DataFrame] = {
    //   if(path!=null)
    //      Some(spark.read.format("csv").option("header", "true").option("inferSchema", "true").load(path))
    //    else
    //      None
    path match {
      case Some(x) => Some(spark.read.format("csv").option("header", "true").option("inferSchema", "true").load(x))
      case None    => None
    }

  }
  def mergDF(x: Option[DataFrame], y: Option[DataFrame]): Option[DataFrame] = {
    (x, y) match {
      case (Some(x), Some(y)) => Some(x.union(y))
      case (Some(x), None)    => Some(x)
      case (None, Some(y))    => Some(y)
      case (None, None)       => None
    }

  }
  def main(args: Array[String]) {

    //    val a1=Array("1","2","3","7").toSet
    //    val a2=Array("1","2","3","4","5").toSet
    //    val a3=a2.diff(a1).toArray

    val spark = SparkSession
      .builder()
      .appName("Broadcast Test").master("local[2]")
      .getOrCreate()
    val df1 = readfile(Some("C:\\Users\\chenwang2017\\git\\Spark-The-Definitive-Guide\\data\\retail-data\\all\\2019-07-28.csv"), spark).get
    val df2 = readfile(Some("C:\\Users\\chenwang2017\\git\\Spark-The-Definitive-Guide\\data\\retail-data\\all\\2019-07-29.csv"), spark).get

    df1.show(false)
    df2.show(false)

    println(df1.count + "  " + df2.count)

    val diff1 = df2.except(df1)
    diff1.show(false)
    diff1.registerTempTable("diffTable1")
    val diff2 = df1.except(df2)
    diff2.show(false)
    diff2.registerTempTable("diffTable2")

    diff1.select("InvoiceNo").intersect(diff2.select("InvoiceNo")).registerTempTable("commonTable")
    spark.sql("select * from diffTable1 where InvoiceNo in (select InvoiceNo from commonTable )").show(false)

    spark.stop()
  }
}
// scalastyle:on println
