package com.woyitech.tags.models.statistics.social

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  短信    最近一年内电话聊天时长
 * 上游源表：
 * dwd_fact_emm_behavior_communication_audit
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  phone_time
 */

object PhoneDurationModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id, cast(round(sum(call_Duration) / 60,2)  as string) phone_time
        |from (
        |         select user_id,call_Duration,data_date,dense_rank() over (partition by user_id order by data_date desc) rp
        |         from (
        |                  select user_id,call_Duration, substr(call_time, 1, 10) data_date
        |                  from dwd_fact_emm_behavior_communication_audit
        |                  where msg_type = 1 and substr(call_time, 1, 10) < substr(current_date, 1, 10)
        |              ) a
        |     ) b
        |where rp <= 365
        |group by user_id
        |
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
