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
package com.spark.tool
import org.apache.spark.SparkConf
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{ approx_count_distinct, expr, max, min, col, collect_list, lit, map }
import org.apache.spark.sql.functions.concat
import java.net.URI
import org.apache.hadoop.fs.{ FileSystem, Path, FileStatus, LocatedFileStatus, RemoteIterator }
/**
 * Usage: Aggregation [partitions] [numElem] [blockSize]
 */
object Aggregation {

  def readfile(path: String, spark: SparkSession): DataFrame = {
    if (path.endsWith("csv")) {
      return spark.read.format("csv").option("header", "true").option("inferSchema", "true").load(path)

    }
    if (path.endsWith("parquet")) {
     return  spark.read.parquet(path)

    }
    return null

  }

  def main(args: Array[String]) {

    // initialize spark context
    val conf = new SparkConf().setAppName("fxconduct etl tool")
    if (!conf.contains("spark.master")) conf.setMaster("local[*]")
    val spark = SparkSession.builder().config(conf).getOrCreate()
    val path1 = "C:\\Users\\chenwang2017\\git\\Spark-The-Definitive-Guide\\data\\retail-data\\all\\2019-07-28.csv"
    val path2 = "C:\\Users\\chenwang2017\\git\\Spark-The-Definitive-Guide\\data\\retail-data\\all\\2019-07-29.csv"
    val keyCols = "InvoiceNo,StockCode"
    val flatColumn = ""
    val outputPath = "C:\\Users\\chenwang2017\\git\\Spark-The-Definitive-Guide\\data\\retail-data\\"
    val keyColumns = keyCols.split(",").toSeq.map(col(_));
    val oldDF0 = readfile(path1, spark)
    val newDF0 = readfile(path2, spark)
    if (!oldDF0.rdd.isEmpty() && !newDF0.rdd.isEmpty()) {
       val oldDF = oldDF0.withColumn("uid", concat(keyColumns: _*))
       val newDF = newDF0.withColumn("uid", concat(keyColumns: _*))
      val deletedOrUpdate = oldDF.except(newDF);
      val addedOrUpdate = newDF.except(oldDF);
      val deletedOrUpdateUid = oldDF.except(newDF).select("uid")
      val addedOrUpdateUid = newDF.except(oldDF).select("uid")

      val updateUid = deletedOrUpdateUid.intersect(newDF.select("uid")).withColumnRenamed("uid", "uuid")
      val deletedUid = deletedOrUpdateUid.withColumnRenamed("uid", "uuid").except(updateUid)
      val addedUid = addedOrUpdateUid.withColumnRenamed("uid", "uuid").except(updateUid)

      val joinExpression_del = oldDF.col("uid") === deletedUid.col("uuid")
      val deletedDF = oldDF.join(deletedUid, joinExpression_del)
      deletedDF.show(false)
      deletedDF.repartition(1).write.parquet(outputPath+"deleted.parquet")
      val joinExpression_add = newDF.col("uid") === addedUid.col("uuid")
      val addedDF = newDF.join(addedUid, joinExpression_add)
      addedDF.show(false)
      addedDF.repartition(1).write.parquet(outputPath+"added.parquet")
      val joinExpression_update_new = newDF.col("uid") === updateUid.col("uuid")
      val updateNewDF = newDF.join(updateUid, joinExpression_update_new)
      updateNewDF.show(false)
      updateNewDF.repartition(1).write.parquet(outputPath+"updated_as.parquet")
      val joinExpression_update_from = oldDF.col("uid") === updateUid.col("uuid")
      val updateOldDF = oldDF.join(updateUid, joinExpression_update_from)
      updateOldDF.show(false)
      val noChange = oldDF.intersect(newDF)
      noChange.show(false)
    }
    spark.stop()
  }
}
// scalastyle:on println
