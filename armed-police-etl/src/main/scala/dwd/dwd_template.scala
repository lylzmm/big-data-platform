package dwd


import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import utils.HiveUtil

object dwd_template {

  def odsToDwd(module: String, sql: String, odsTableName: String, dwdTableName: String) = {
    System.out.println("======================= " + odsTableName + " to  ============== " + dwdTableName)
    if ("dim" == module) {
      // 维度表
      println("=======================  维度表")
      val conf = new SparkConf().setMaster("local[*]").setAppName("DwdTemplate")
      val spark = SparkSession.builder.enableHiveSupport.config(conf).getOrCreate
      // 最大分区
      val maxPartition = HiveUtil.getMaxPartition(odsTableName)
      val sql = s"select * from ${odsTableName} where ds = ${maxPartition}"
      spark.sql(sql).drop("ds").coalesce(1).write.mode(SaveMode.Overwrite).insertInto(dwdTableName)
      spark.close()
    } else if ("fact" == module) {
      // 事实表
      println("=======================  事实表")
      val conf = new SparkConf().setAppName("DwdTemplate")
      val spark = SparkSession.builder.enableHiveSupport.config(conf).getOrCreate
      spark.sql(sql).coalesce(1).write.mode(SaveMode.Overwrite).insertInto(dwdTableName)
      spark.close()
    }
  }

}
