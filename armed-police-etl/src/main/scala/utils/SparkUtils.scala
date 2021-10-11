package utils

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkUtils {

  def createSparkSession(isLocal: Boolean, isHive: Boolean, className: String): SparkSession = {
    var spark: SparkSession = null
    if (isLocal && isHive) {
      // 本地hive
      val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName(className)
      spark = SparkSession.builder.enableHiveSupport.config(conf).getOrCreate
    } else if (!isLocal && isHive) {

      val conf: SparkConf = new SparkConf().setAppName(className)
      spark = SparkSession.builder.enableHiveSupport.config(conf).getOrCreate
    } else if (!isLocal && !isHive) {
      val conf: SparkConf = new SparkConf().setAppName(className)
      spark = SparkSession.builder().config(conf).getOrCreate()
    }
    spark
  }
}
