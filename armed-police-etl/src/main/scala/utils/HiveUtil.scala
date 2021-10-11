package utils

import org.apache.spark.sql.{DataFrame, SparkSession}

import java.sql.{Connection, DriverManager, PreparedStatement, SQLException}
import scala.collection.mutable

object HiveUtil {

  private val driverName = "org.apache.hive.jdbc.HiveDriver"
  private val url = "jdbc:hive2://node01:10000"
  private val user = "hadoop"
  private val pwd = ""
  private var con: Connection = _
  private var ps: PreparedStatement = _


  def conn(): Connection = {
    try {
      Class.forName(driverName)
      con = DriverManager.getConnection(url, user, pwd)
    } catch {
      case e: ClassNotFoundException =>
        e.printStackTrace()
      case e: SQLException =>
        e.printStackTrace()
    }
    con
  }


  def getLastValue(sql: String) = {
    val con = conn()
    var lastValue: Integer = null
    try {
      ps = con.prepareStatement(sql)
      val resultSet = ps.executeQuery
      val list = new mutable.TreeSet[Integer]
      while (resultSet.next) {
        val string = resultSet.getString(1)
        list.add(string.replace("ds=", "").toInt)
      }
      lastValue = list.last
    }
    lastValue
  }


  def close(con: Connection, ps: PreparedStatement) = {
    if (null != conn)
      try con.close()
      catch {
        case e: SQLException =>
          e.printStackTrace()
      }

    if (ps != null)
      try con.close()
      catch {
        case e: SQLException =>
          e.printStackTrace()
      }
  }

  /**
   * 调大最大分区个数
   *
   * @return
   */
  def setMaxPartitions(spark: SparkSession): DataFrame = {
    spark.sql("set hive.exec.dynamic.partition=true")
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.sql("set hive.exec.max.dynamic.partitions=100000")
    spark.sql("set hive.exec.max.dynamic.partitions.pernode=100000")
    spark.sql("set hive.exec.max.created.files=100000")
  }

  /**
   * 开启压缩
   *
   * @return
   */
  def openCompression(spark: SparkSession): DataFrame = {
    spark.sql("set mapred.output.compress=true")
    spark.sql("set hive.exec.compress.output=true")
  }

  /**
   * 开启动态分区，非严格模式
   *
   */
  def openDynamicPartition(spark: SparkSession): DataFrame = {
    spark.sql("set hive.exec.dynamic.partition=true")
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
  }

  /**
   * 使用lzo压缩
   */
  def useLzoCompression(spark: SparkSession): DataFrame = {
    spark.sql("set io.compression.codec.lzo.class=com.hadoop.compression.lzo.LzoCodec")
    spark.sql("set mapred.output.compression.codec=com.hadoop.compression.lzo.LzopCodec")
  }

  /**
   * 使用snappy压缩
   */
  def useSnappyCompression(spark: SparkSession): DataFrame = {
    spark.sql("set mapreduce.map.output.compress.codec=org.apache.hadoop.io.compress.SnappyCodec");
    spark.sql("set mapreduce.output.fileoutputformat.compress=true")
    spark.sql("set mapreduce.output.fileoutputformat.compress.codec=org.apache.hadoop.io.compress.SnappyCodec")
  }


  /**
   * 获取最大分区
   */
  def getMaxPartition(tableName: String): Integer = {
    val sql = s"show partitions $tableName"
    getLastValue(sql)
  }

  /**
   * 获取最大分区的里的最小id
   */

  def getMaxPartitionMinId(tableName: String, id: String): Integer = {
    val maxPartition = getMaxPartition(tableName)
    val sql = s"select min($id) from $tableName where ds = $maxPartition"
    println(sql)
    getLastValue(sql)
  }
}
