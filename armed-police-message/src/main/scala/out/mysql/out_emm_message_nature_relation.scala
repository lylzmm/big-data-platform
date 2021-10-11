package out.mysql

import org.apache.spark.sql.SaveMode
import utils.{JdbcUtils, SparkUtils}

import java.util.Properties

object out_emm_message_nature_relation {

  def main(args: Array[String]): Unit = {
    val connection = JdbcUtils.getConnection("ahwuj_config.properties", 3307)

    val tableName :String = "emm_message_nature_relation"
    // 保存前清空
    connection.prepareStatement(s"truncate table $tableName").execute()

    try {
      val spark = SparkUtils.createSparkSession(isLocal = true, isHive = true, "out_emm_message_nature_relation")

      val pro = new Properties
      pro.put("user", JdbcUtils.getUser)
      pro.put("password", JdbcUtils.getPassword)

      val sql =
        """
          |select id, msgId, natureId, sms_content
          |from ads_emm_message_nature_relation
          |""".stripMargin

      spark.sql(sql).write.mode(SaveMode.Append).jdbc(JdbcUtils.getUrl, tableName, pro)
    } finally
      JdbcUtils.close(connection)
  }


}
