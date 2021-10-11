package ads

import org.apache.spark.sql.SaveMode
import utils.SparkUtils

object ads_emm_message_nature_relation {
  def main(args: Array[String]): Unit = {

    val savaTableName = "ads_emm_message_nature_relation"

    val session = SparkUtils.createSparkSession(isLocal = false, isHive = true, savaTableName)
    val sql =
      """
        |select t1.id, t1.msgId, t1.natureId, ' ' as sms_content
        |from (
        |         SELECT uuid() id
        |              , msgId
        |              , id     natureId
        |         FROM ads_emm_message_nature_group lateral VIEW explode(split(msgIds, ",")) movie_info_tmp AS msgId
        |     ) t1,
        |     dws_emm_message_nature_group_duplicate t2
        |where t1.msgId = t2.id
        |""".stripMargin

    session.sql(sql).write.mode(SaveMode.Overwrite).insertInto(savaTableName)

    session.close()
  }
}
