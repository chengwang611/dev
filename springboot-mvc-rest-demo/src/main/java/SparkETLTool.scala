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
import org.apache.spark.sql.functions.to_json
import java.net.URI
import java.util.concurrent.{ ConcurrentHashMap, ConcurrentMap }
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
      return spark.read.parquet(path)

    }
    return null

  }

  def main(args: Array[String]) {
    val vars = new ConcurrentHashMap[String, String]
    args.filter(_.indexOf('=') > 0).foreach { arg =>
      val pos = arg.indexOf('=')
      vars.put(arg.substring(0, pos), arg.substring(pos + 1))
    }
    // initialize spark context
    val conf = new SparkConf().setAppName("fxconduct etl tool")
    if (!conf.contains("spark.master")) conf.setMaster("local[*]")
    val spark = SparkSession.builder().config(conf).getOrCreate()
    val path1 = vars.getOrDefault("path1", "C:\\Users\\chenwang2017\\git\\Spark-The-Definitive-Guide\\data\\retail-data\\all\\day1.csv")
    val path2 = vars.getOrDefault("path2", "C:\\Users\\chenwang2017\\git\\Spark-The-Definitive-Guide\\data\\retail-data\\all\\day3.csv")
    val keyCols = vars.getOrDefault("keyCols", "InvoiceNo,StockCode")
    val flatColumn = vars.getOrDefault("flatColumn", "")
    val outputPath = vars.getOrDefault("outputPath", "C:\\Users\\chenwang2017\\git\\Spark-The-Definitive-Guide\\data\\retail-data\\etl\\" + System.currentTimeMillis() + "\\")
    val keyColumns = keyCols.split(",").toSeq.map(col(_));
    val oldDF = readfile(path1, spark)
    val newDF = readfile(path2, spark)
    if (oldDF != null && newDF != null && !oldDF.rdd.isEmpty() && !newDF.rdd.isEmpty()) {
      var oldDF0 = oldDF.withColumn("uid", concat(keyColumns: _*)).cache()
      var newDF0 = newDF.withColumn("uid", concat(keyColumns: _*)).cache()
      if (!"".equalsIgnoreCase(flatColumn)) {
        oldDF0 = oldDF0.withColumn(flatColumn + "_json", to_json(col(flatColumn))).drop(flatColumn)
        newDF0 = newDF0.withColumn(flatColumn + "_json", to_json(col(flatColumn))).drop(flatColumn)
      }

      val deletedOrUpdateUid = oldDF0.except(newDF0).select("uid")
      System.out.println("deletedOrUpdate :")
      oldDF0.except(newDF0).show(false)
      val addedOrUpdateUid = newDF0.except(oldDF0).select("uid")
      System.out.println("add OrUpdate :")
      newDF0.except(oldDF0).show(false)
      //get ids for update,add and delete
      val updateUid = deletedOrUpdateUid.intersect(addedOrUpdateUid).withColumnRenamed("uid", "uuid")
      val deletedUid = deletedOrUpdateUid.withColumnRenamed("uid", "uuid").except(updateUid)
      val addedUid = addedOrUpdateUid.withColumnRenamed("uid", "uuid").except(updateUid)
      //fetch the raw dataframe
      val oldDFwithUid = oldDF.withColumn("uid", concat(keyColumns: _*))
      val newDFwithUid = newDF.withColumn("uid", concat(keyColumns: _*))

      //deleted records in raw file
      val joinExpression_del = oldDFwithUid.col("uid") === deletedUid.col("uuid")
      val deletedDF = oldDFwithUid.join(deletedUid, joinExpression_del)
      System.out.println("deleted:")
      deletedDF.show(false)
      deletedDF.drop("uid").drop("uuid").repartition(1).write.parquet(outputPath + "deleted.parquet")

      //added records in raw
      val joinExpression_add = newDFwithUid.col("uid") === addedUid.col("uuid")
      val addedDF = newDFwithUid.join(addedUid, joinExpression_add)
      System.out.println("added:")
      addedDF.show(false)
      addedDF.drop("uid").drop("uuid").repartition(1).write.parquet(outputPath + "added.parquet")
      //updated records in the new file
      val joinExpression_update_new = newDFwithUid.col("uid") === updateUid.col("uuid")
      val updateNewDF = newDFwithUid.join(updateUid, joinExpression_update_new)
      System.out.println("update as :")
      updateNewDF.show(false)
      updateNewDF.drop("uid").drop("uuid").repartition(1).write.parquet(outputPath + "updated_as.parquet")
      //updated records in the old file
      val joinExpression_update_from = oldDFwithUid.col("uid") === updateUid.col("uuid")
      val updateOldDF = oldDFwithUid.join(updateUid, joinExpression_update_from)
      System.out.println("updated from :")
      updateOldDF.show(false)

      //no change part
      val joinType = "left_anti"
      val joinExpression_update_add = newDFwithUid.col("uid") === addedOrUpdateUid.col("uid")

      val noChange_left_anti = newDFwithUid.join(addedOrUpdateUid, joinExpression_update_add, joinType)
      System.out.println("left_anti -no change :")
      noChange_left_anti.show(false)
      //
      //      val noChange = newDFwithUid.except(addedDF.drop("uuid")).except(updateNewDF.drop("uuid")).drop("uid")
      //      System.out.println("no change :")
      //      noChange.show(false)
      System.out.println("old  =" + oldDF.count)
      System.out.println("new  =" + newDF.count)
      System.out.println("deleted =" + deletedDF.count)
      System.out.println("added =" + addedDF.count)
      System.out.println("update new =" + updateNewDF.count)
      System.out.println("update old  =" + updateOldDF.count)
      //      System.out.println("no change =" + noChange.count)
      System.out.println(" left-anti-no change =" + noChange_left_anti.count)
    }

    spark.stop()
  }
}
// scalastyle:on println
