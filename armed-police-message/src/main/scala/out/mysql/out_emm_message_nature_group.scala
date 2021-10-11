package out.mysql

import org.apache.spark.sql.SaveMode
import utils.{JdbcUtils, SparkUtils}

import java.util.Properties

object out_emm_message_nature_group {

  def main(args: Array[String]): Unit = {

    val connection = JdbcUtils.getConnection("ahwuj_config.properties", 3305)

    val tableName :String = "emm_message_nature_group"

    // 保存前清空
    connection.prepareStatement(s"truncate table $tableName").execute()

    try {
      val spark = SparkUtils.createSparkSession(isLocal = true, isHive = true, "out_ads_emm_message_nature_group")

      val pro = new Properties
      pro.put("user", JdbcUtils.getUser)
      pro.put("password", JdbcUtils.getPassword)

      val frame = spark.read.jdbc(JdbcUtils.getUrl, "emm_message_nature_new", pro)
      frame.createOrReplaceTempView("emm_message_nature_new")

      val sql =
        """
          |select t1.id,
          |       groupCode,
          |       loginName,
          |       staffName,
          |       t1.msgOriginName,
          |       t1.msgNature,
          |       cashNum,
          |       yearMonth,
          |       t2.id nature_new_id
          |from ads_emm_message_nature_group t1, emm_message_nature_new t2
          |where t1.msgOriginName = t2.msgOriginName and t1.msgNature = t2.msgNature
          |""".stripMargin


      spark.sql(sql).write.mode(SaveMode.Append).jdbc(JdbcUtils.getUrl, tableName, pro)
    } finally
      JdbcUtils.close(connection)
  }

}
