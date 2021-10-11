package com.woyitech.tags.dao

import com.woyitech.tags.tools.HBaseTools
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import java.util.Properties

/**
 * 读取HBase中数据同步到mysql中
 */
object HBaseToMysql {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("HBaseToMysql ")
    val session = SparkSession.builder.config(sparkConf).getOrCreate

    val cols = Seq(
      // 用户基本属性
      "user_id",
      "device_number",
      "wechat_number",
      "mobile_qq_number",
      "wife",
      "house_loan_money",
      "car_loan_money",
      // 上网行为
      "sensitive_word_top5",
      "sensitive_word_number",
      "stay_up_late_day",
      // 社交属性
      "phone_time",
      "linkman_number",
      "wechat_video_time",
      "wechat_phone_time",
      "phone_continuous_7_day_number",
      // 心理属性
      "be_rejected_phone_number",
      "refused_to_phone_number",
      "wechat_be_rejected_number",
      "wechat_refused_to_number",
      "receive_negative_phrase",
      "send_negative_phrase",
      "negative_number",
      "wechat_receive_negative_phrase",
      "wechat_send_negative_phrase",
      "wechat_negative_number",
      "wechat_linkman_phrase",
      // 消费属性
      "wechat_transfer_out_total",
      "wechat_to_change_into_total",
      "wechat_red_packet_send_total",
      "wechat_red_packet_closed_total")
    val df: DataFrame = HBaseTools.read(session, "node02", "2181", "tbl_profile", "user", cols)

    val pro: Properties = new Properties
    pro.put("user", "root")
    pro.put("password", "000000")
    df.write.mode(SaveMode.Overwrite).jdbc("jdbc:mysql://node01:3306/test?characterEncoding=utf8&useSSL=false", "tbl_profile", pro)


    session.close()
  }
}
