package dws

import org.apache.spark.sql.SaveMode
import utils.SparkUtils

object dws_emm_message_nature_group {

  def main(args: Array[String]): Unit = {

    val savaTableName = "dws_emm_message_nature_group"
    val session = SparkUtils.createSparkSession(isLocal = false, isHive = true, "dws_emm_message_nature_group")
    session.sql("add jar /opt/jar/armed-police-message-jar-with-dependencies.jar")
    session.sql("create temporary function MessageUDF as 'udf.MessageUDF'")
    session.sql("create temporary function DueDateUDF as 'udf.DueDateUDF'")
    
    val sql =
      """
        |select id
        |       ,user_id
        |       ,device_id
        |       ,call_name
        |       ,call_phone
        |       ,call_time
        |       ,sms_content
        |       ,split(MessageUDF(sms_content), '->')[0] msgOriginName
        |       ,split(MessageUDF(sms_content), '->')[1] msgNature
        |       ,split(MessageUDF(sms_content), '->')[2] money
        |       ,if(DueDateUDF(sms_content) != '', if(size(split(DueDateUDF(sms_content), '-')) < 3, substr(concat(substr(call_time, 1, 4),'-',DueDateUDF(sms_content)), 1, 7) , substr(DueDateUDF(sms_content),1 ,7)), substr(call_time, 1, 7)) due_date
        |from dwd_fact_emm_behavior_communication_audit
        |where substr(call_time, 1, 10) >= '2021-01-01' and
        |        call_type = 1
        |  and msg_type = 2
        |  and call_phone != '10000'
        |  and call_phone != '10010'
        |  and call_phone != '10086'
        |  and (
        |            sms_content like '%支出%' or
        |            sms_content like '%收入%' or
        |            sms_content like '%借款%' or
        |            sms_content like '%取现到账%' or
        |            sms_content like '%还款%' or
        |            sms_content like '%房贷%'
        |    );
        |""".stripMargin

    session.sql(sql).coalesce(1).write.mode(SaveMode.Overwrite).insertInto(savaTableName)

    session.close()
  }
}
