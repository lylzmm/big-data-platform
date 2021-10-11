package com.woyitech.tags.models.statistics.basic

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  短信    是否有老婆
 * 上游源表：
 * dwd_fact_emm_behavior_communication_audit
 * dws_emm_behavior_communication_max_data_date
 *
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  wife
 */

object WifeModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(t1.user_id as string), max(call_phone) wife
        |from (select user_id, call_name, sms_content, call_time, if(instr(call_phone,'+') !=0,substr(call_phone,4,length(call_phone)),call_phone) call_phone
        |       from dwd_fact_emm_behavior_communication_audit
        |      ) t1, dws_emm_behavior_communication_max_data_date t2
        |where (call_name like '%老婆%' or sms_content like '%老婆%') and length(call_phone) = 11 and t1.user_id = t2.user_id and t1.call_time >= date_sub(data_date, 365)
        |group by t1.user_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
