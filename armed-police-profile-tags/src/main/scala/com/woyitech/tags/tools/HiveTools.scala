package com.woyitech.tags.tools

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object HiveTools {
  def write(sql: String, hiveTable: String): Unit = {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("HiveTools")
    val session = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()


    // 写入表
    session.sql(sql).coalesce(1).write.mode(SaveMode.Overwrite).insertInto(hiveTable)

    session.close()
  }


}
