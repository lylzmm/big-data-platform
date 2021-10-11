package dwd.fact

import dwd.dwd_template
import utils.SparkUtils


object dwd_dim_emm_behavior_chat_audit_all {

  def main(args: Array[String]): Unit = {

    val spark = SparkUtils.createSparkSession(isLocal = false, isHive = true, "dwd_dim_emm_behavior_chat_audit_all")
    spark.sql("create temporary function MessageUDF as 'udf.MessageUDF'")
    spark.sql("create temporary function DueDateUDF as 'udf.DueDateUDF'")
    val sql =
      """
        |select max(id)            id
        |     , user_id
        |     , max(device_id)     device_id
        |     , max(call_name)     call_name
        |     , call_phone
        |     , max(call_city)     call_city
        |     , call_type
        |     , call_time
        |     , max(call_duration) call_duration
        |     , sms_content
        |     , max(mms_content)   mms_content
        |     , max(msg_type)      msg_type
        |     , max(theme)         theme
        |     , max(file_name)     file_name
        |     , max(file_path)     file_path
        |     , max(msg_id)        msg_id
        |     , max(create_time)   create_time
        |     , max(modify_time)   modify_time
        |     , max(ds)            ds
        |from ods_emm_behavior_communication_audit
        |group by user_id, call_phone, call_type, call_time, sms_content
        |""".stripMargin

    println(sql)

    dwd_template.odsToDwd("fact", sql, "ods_emm_behavior_communication_audit", "dwd_fact_emm_behavior_communication_audit")
  }
}
