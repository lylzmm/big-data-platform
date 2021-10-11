package hive

import org.apache.spark.SparkConf
import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import utils.ConfigurationUtils.readSql


object SaveHive {

  def main(args: Array[String]): Unit = {
    val fileName: String = args(0)
    val hiveTableName: String = args(1)

    val conf: SparkConf = new SparkConf().setAppName("ReadHive")
    val spark: SparkSession = SparkSession.builder.enableHiveSupport.config(conf).getOrCreate

    // 读取sql
    val sql = readSql(fileName)

    System.out.println("=========================================================================================")
    System.out.println(sql.toString())
    System.out.println("=========================================================================================")

    val dataset: Dataset[Row] = spark.sql(sql.toString)
    // 缓存
    dataset.cache
    dataset.coalesce(1).write.mode(SaveMode.Overwrite).insertInto(hiveTableName)

    spark.close()
  }

}
