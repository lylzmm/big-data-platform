package ads

import org.apache.spark.sql.SaveMode
import utils.SparkUtils

object ads_message_emm_train_ticket {
  def main(args: Array[String]): Unit = {

    val savaTableName = "ads_message_emm_train_ticket"

    val session = SparkUtils.createSparkSession(isLocal = false, isHive = true, savaTableName)

    session.sql("add jar /opt/jar/armed-police-train-ticket-jar-with-dependencies.jar")
    session.sql("create temporary function TrainTicketUDF as 'udf.TrainTicketUDF'")
    val sql =
      """
        |select user_id,
        |       call_time,
        |       source,
        |       wayToTravel,
        |       `date`,
        |       shuttleBus,
        |       stand,
        |       sms_content
        |from (
        |         select user_id,
        |                call_time,
        |
        |                split(TrainTicketUDF(sms_content, call_time), '\\|')[0] as source,
        |                split(TrainTicketUDF(sms_content, call_time), '\\|')[1] as wayToTravel,
        |                split(TrainTicketUDF(sms_content, call_time), '\\|')[2] as `date`,
        |                split(TrainTicketUDF(sms_content, call_time), '\\|')[3] as shuttleBus,
        |                split(TrainTicketUDF(sms_content, call_time), '\\|')[4] as stand,
        |                sms_content
        |         from ods_emm_behavior_communication_audit
        |         where ds >= 20210101
        |           and sms_content like '%火车票%'
        |           and sms_content not like '%航空%'
        |           and substr(call_time, 1, 10) > '2021-01-01'
        |     ) t1
        |where source != ''
        |""".stripMargin

    session.sql(sql).coalesce(1).write.mode(SaveMode.Overwrite).insertInto(savaTableName)

    session.close()
  }
}
