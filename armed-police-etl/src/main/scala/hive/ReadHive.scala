package hive


import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import utils.ConfigurationUtils.readSql


object ReadHive {

  def main(args: Array[String]): Unit = {
    val name = args(0)
    val conf = new SparkConf().setAppName("ReadHive")
    val spark = SparkSession.builder.enableHiveSupport.config(conf).getOrCreate

    // 读取sql
    val sql = readSql(name)

    System.out.println("=========================================================================================")
    System.out.println(sql.toString)
    System.out.println("=========================================================================================")

    spark.sql(sql.toString).show(10000, 300)
    spark.close()
  }
}
