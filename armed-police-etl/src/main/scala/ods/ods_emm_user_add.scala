package ods


import org.apache.spark.sql.{Dataset, Row, SaveMode, functions}
import utils.SparkUtils
import utils.{JdbcUtils, StringUtil}

import java.sql.Connection
import java.util.Properties

object ods_emm_user_add {

  def main(args: Array[String]): Unit = {
    System.out.println("===================================================: 每日全量  " + "emm_user")

    // 跳转
    val connection: Connection = JdbcUtils.getConnection("ahwuj_config.properties", 3306)

    try {
      val spark = SparkUtils.createSparkSession(isLocal = true, isHive = true, "ods_emm_user_add")
      val pro: Properties = new Properties
      pro.put("user", JdbcUtils.getUser)
      pro.put("password", JdbcUtils.getPassword)

      val dataset = spark.read.jdbc(JdbcUtils.getUrl, "emm_user", pro)
      val ds: Integer = StringUtil.getCurrentDay.replace("-", "").toInt
      dataset.coalesce(1).withColumn("ds", functions.lit(ds)).write.mode(SaveMode.Overwrite).insertInto("ods_emm_user")

      // 关闭
      spark.close()
    } finally {

      // 关闭
      JdbcUtils.close(connection)
    }
  }


}
