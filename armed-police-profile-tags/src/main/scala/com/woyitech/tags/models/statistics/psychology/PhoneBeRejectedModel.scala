package com.woyitech.tags.models.statistics.psychology

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  短信    被拒电话次数
 * 上游源表：
 * dwd_fact_emm_behavior_communication_audit
 * dws_emm_behavior_communication_max_data_date
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  be_rejected_phone_number
 * 字段说明：  msg_type  1:通话,2:短信   |    call_type  通话：1被叫, 2:主叫'
 */

object PhoneBeRejectedModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id, cast(sum(ct) as string) be_rejected_phone_number
        |from (
        |         select user_id, data_date, call_phone, call_type, count(*) ct
        |         from (
        |                  select user_id, data_date, call_phone, call_type
        |                  from (
        |                           select t1.user_id, call_phone, call_type, substr(call_time, 1, 10) data_date
        |                           from dwd_fact_emm_behavior_communication_audit t1, dws_emm_behavior_communication_max_data_date t2
        |                           where msg_type = 1 and call_Duration = '0' and call_type = 2 and t1.user_id = t2.user_id and substr(t1.call_time,1 ,10) >= date_sub(t2.data_date, 365)
        |                       ) b
        |                  where  call_phone != '10086' and call_phone != '10000' and call_phone != '95566'
        |              ) c
        |         group by user_id,data_date,call_phone,call_type
        |         having count(*) > 3
        |     ) d
        |group by user_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
