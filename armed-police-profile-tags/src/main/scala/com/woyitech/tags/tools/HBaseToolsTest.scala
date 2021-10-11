//package com.woyitech.tags.tools
//
//import org.apache.spark.SparkConf
//import org.apache.spark.sql.{DataFrame, SparkSession}
//
//
//object HBaseToolsTest {
//  def main(args: Array[String]): Unit = {
//    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SMS").set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
//    val spark = SparkSession.builder.config(sparkConf).getOrCreate
//
//
//    val cols = Seq("user_id", "device_number",
////      "wechat_number",
////      "mobileqq_number",
////      "sensitive_word_top5",
////      "stay_up_late_day",
////      "phone_time",
////      "be_rejected_phone_number",
////      "refused_to_phone_number",
////      "wechat_be_rejected_number",
////      "wechat_refused_to_number",
////      "wechat_transfer_out_total",
////      "wechat_to_change_into_total",
////      "wechat_red_packet_send_total",
////      "wechat_red_packet_closed_total",
//    "receive_negative_phrase",
//    "send_negative_phrase",
//    "negative_number")
//    val df: DataFrame = HBaseTools.read(spark, "node02", "2181", "tbl_profile", "user", cols)
//    df.printSchema()
//    df.show(10000, truncate = false)
//
//
//  }
//}
