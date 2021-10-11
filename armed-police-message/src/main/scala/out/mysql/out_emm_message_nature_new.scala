package out.mysql

import org.apache.spark.sql.SaveMode
import utils.{JdbcUtils, SparkUtils}

import java.util.Properties

object out_emm_message_nature_new {

  def main(args: Array[String]): Unit = {

    val connection = JdbcUtils.getConnection("ahwuj_config.properties", 3304)
    val spark = SparkUtils.createSparkSession(isLocal = true, isHive = true, "ads_emm_message_source_history_out")

    val pro = new Properties
    pro.put("user", JdbcUtils.getUser)
    pro.put("password", JdbcUtils.getPassword)

    val frame = spark.read.jdbc(JdbcUtils.getUrl, "emm_message_nature_new", pro)
    frame.createOrReplaceTempView("emm_message_nature_new")
    spark.sql(
      """
        |select uuid() id, t1.msgOriginName, t1.msgNature, concat(current_date(),' 00:00:00') createTime, '0' edited,'1' status,  msgNewName
        |from (
        |         select msgOriginName, msgNature from ads_emm_message_nature_group group by msgOriginName, msgNature
        |     ) t1
        |         left join emm_message_nature_new t2 on t1.msgNature = t2.msgNature and t1.msgOriginName = t2.msgOriginName
        |where t2.msgOriginName is null
        |""".stripMargin).write.mode(SaveMode.Append).jdbc(JdbcUtils.getUrl, "emm_message_nature_new", pro)
    JdbcUtils.close(connection)
  }

}
