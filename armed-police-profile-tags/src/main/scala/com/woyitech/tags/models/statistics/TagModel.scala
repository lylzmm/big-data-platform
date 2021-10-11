package com.woyitech.tags.models.statistics

import com.woyitech.tags.tools.HBaseTools
import org.apache.hadoop.hbase.client.{Put, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object TagModel {
  def saveTags(sql: String): Unit = {
    val sparkConf: SparkConf = new SparkConf()
//      .setMaster("local[*]")
    // a. 创建SparkConf,设置应用相关配置
    sparkConf.setAppName(this.getClass.getSimpleName.stripSuffix("$")).set("spark.serializer", "org.apache.spark.serializer.KryoSerializer").registerKryoClasses(Array(classOf[ImmutableBytesWritable], classOf[Result], classOf[Put]))
    val session = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()

    session.sql("add jar /opt/jar/armed-police-message-jar-with-dependencies.jar")
    session.sql("create temporary function MessageUDF as 'udf.MessageUDF'")

    // 读取标签
    val frame = session.sql(sql)

    frame.show(1000)

    // 保存df
    HBaseTools.write(frame, "node02", "2181", "tbl_profile", "user", "user_id")

    // 关闭
    session.close()
  }
}
