package dwd.fact

import dwd.dwd_template
import utils.HiveUtil


object dwd_dim_emm_behavior_chat_audit_add {

  def main(args: Array[String]): Unit = {

    val startId = HiveUtil.getMaxPartitionMinId("dwd_fact_emm_behavior_communication_audit", "id")
    val maxPartition = HiveUtil.getMaxPartition("dwd_fact_emm_behavior_communication_audit")

    val sql =
      s"""
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
        |where ds >= ${maxPartition} and id >= ${startId}
        |group by user_id, call_phone, call_type, call_time, sms_content
        |""".stripMargin

    println(sql)

    dwd_template.odsToDwd("fact", sql, "ods_emm_behavior_communication_audit", "dwd_fact_emm_behavior_communication_audit")
  }
}
