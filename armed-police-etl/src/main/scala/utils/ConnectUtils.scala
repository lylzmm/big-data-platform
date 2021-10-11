package utils

import java.sql.{Connection, DriverManager}
import java.util.Properties
import org.apache.spark.sql.{DataFrame, SaveMode}

object ConnectUtils {

  private val properties: Properties = PropertiesUtils.getProperties

  /**
   * mysql数据源输入
   */
  def mysqlSource: (String) =>
    DataFrame = (tableName: String) => {
    val prop = new Properties()
    prop.setProperty("user", properties.getProperty("mysql.user"))
    prop.setProperty("password", properties.getProperty("mysql.password"))
    prop.setProperty("driver", "com.mysql.jdbc.Driver")

    //是否开启读大表配置
    if (properties.getProperty("mysql.isPartition").equals("true")) {
      //表数据大小
      val tableCount: Int = properties.getProperty("mysql.tableCount").toInt
      //每页数据大小
      val partitionLimit: Int = properties.getProperty("mysql.partitionLimit").toInt
      //需要的分页数
      val pages: Int = tableCount / partitionLimit
      //分页条件
      val partitionArray = new Array[String](pages)
      val orderField: String = properties.getProperty("mysql.orderField")
      for (i <- 0 until pages) {
        //        partitionArray(i) = s"1=1 order by ${properties.getProperty("mysql.orderField")} limit ${i * partitionLimit},${partitionLimit}"
        // 考虑到mysql在超大数据量查询时limit的性能问题，建议用这种方式进行limit分页
        partitionArray(i) = s"1=1 and ${orderField} >=(select ${orderField} from ${tableName} order by ${orderField} limit ${i * partitionLimit},1) limit ${partitionLimit}"
      }
      SparkUtils.createSparkSession(isLocal = false, isHive = true, "ConnectUtils").read.jdbc(properties.getProperty("mysql.url"), tableName, partitionArray, prop)
    } else {
      SparkUtils.createSparkSession(isLocal = false, isHive = true, "ConnectUtils").read.jdbc(properties.getProperty("mysql.url"), tableName, prop)
    }
  }
}
