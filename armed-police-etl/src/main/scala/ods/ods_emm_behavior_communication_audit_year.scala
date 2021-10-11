package ods

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

object ods_emm_behavior_communication_audit_year {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("ods_emm_behavior_communication_audit_year")
    val session = SparkSession.builder.enableHiveSupport.config(conf).getOrCreate
    session.sql("set hive.exec.dynamic.partition=true;")
    session.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    session.sql("set hive.exec.max.dynamic.partitions=20000;")
    val dataFrame = session.sql("select *, substr(create_time, 1, 4) year from ods_emm_behavior_communication_audit").where("year = '2019'")

      .drop("ds")


    dataFrame.coalesce(1).write.mode(SaveMode.Overwrite).insertInto("ods_emm_behavior_communication_audit_year")

  }
}
