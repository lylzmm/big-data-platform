package ods

import org.apache.spark.SparkConf
import org.apache.spark.sql._
import utils.{HiveUtil, JdbcUtils, KerberosUtils, PropertyUtils, StringUtil}

import java.sql.Connection
import java.util.Properties

object ods_template {

  @throws[Exception]
  def mysql_to_hive_ods(module: String, db_tableName: String, id: String, hive_tableName: String, partition: String): Unit = {
    // 认证
    // cluster
    println("开始认证用户")
    KerberosUtils.loginUserFromKeytab("cluster")


    // 1 开启跳转
    val connection: Connection = JdbcUtils.getConnection()
    try {
      // 2 创建spark对象
      val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("ods_template")
      val spark: SparkSession = SparkSession.builder.enableHiveSupport.config(conf).getOrCreate
      val pro: Properties = new Properties
      pro.put("user", PropertyUtils.getProperty("name"))
      pro.put("password", PropertyUtils.getProperty("password"))

      // 4 增量开始位置
      var startId: Integer = 0
      var lastPartition: Integer = 0
      if ("add" == module) {
        startId = HiveUtil.getMaxPartitionMinId(hive_tableName, id)
        System.out.println(startId)
        lastPartition = HiveUtil.getMaxPartition(hive_tableName)
      }

      // 5 spark读取mysql
      var dataset: Dataset[Row] = null
      if ("add" == module && startId != null) {
        System.out.println("===================================================: 每日增量  " + db_tableName + "  startId " + startId)
        dataset = spark.read.jdbc(JdbcUtils.getUrl, db_tableName, pro).where(id + " >= " + startId)
        dataset.createOrReplaceTempView(db_tableName)
        val sql: String = "select * from ( select *,replace(substr(" + partition + ",1,10),'-','') ds from " + db_tableName + " ) a where ds >= " + lastPartition + ""
        System.out.println(sql)
        // 设置动态分区
        HiveUtil.openDynamicPartition(spark)
        spark.sql(sql).coalesce(1).write.mode(SaveMode.Overwrite).insertInto(hive_tableName)
      } else if ("all" == module) {
        System.out.println("===================================================:   " + db_tableName)
        dataset = spark.read.jdbc(JdbcUtils.getUrl, db_tableName, pro)
        dataset.show()
        val ds: Integer = StringUtil.getCurrentDay.replace("-", "").toInt
        dataset.coalesce(1).withColumn("ds", functions.lit(ds)).write.mode(SaveMode.Overwrite).insertInto(hive_tableName)
      }
    }
    finally {

      // 关闭
      JdbcUtils.close(connection)
    }
  }

}
