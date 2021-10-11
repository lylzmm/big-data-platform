package com.woyitech.tags.models.statistics.social

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  短信    最近一年内短信联系人个数
 * 上游源表：
 * dwd_fact_emm_behavior_communication_audit
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  linkman_number
 */

object PhoneLinkmanNumberModel {

  def main(args: Array[String]): Unit = {
    val sql =
      """
        |select cast(user_id as string) user_id, cast(count(*) as string) linkman_number
        |from (
        |         select user_id, call_name
        |         from (
        |                  select user_id, call_name, call_time,if(substr(max_date, 1, 10) > current_date, current_date, substr(max_date, 1, 10)) max_date
        |                  from (
        |                           select user_id, call_name, call_time, max(call_time) over (partition by user_id) max_date
        |                           from dwd_fact_emm_behavior_communication_audit t1
        |
        |                       ) a
        |              ) b
        |         where substr(call_time,1 , 10) >= date_sub(max_date, 365)
        |         group by user_id, call_name
        |) c
        |group by user_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
