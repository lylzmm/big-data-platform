package com.woyitech.tags.models.statistics.behavior

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  短信和微信    最近一年熬夜天数
 * 上游源表：
 * dwd_fact_emm_behavior_communication_audit
 * dwd_fact_emm_behavior_chat_log
 * dwd_dim_emm_behavior_chat_audit
 * dwd_dim_emm_pub_staff
 *
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  stay_up_late_day
 */

object StayUpLateModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id,cast(count(*) as string) stay_up_late_day
        |from (
        |         select user_id,data_date
        |         from (
        |                  -- 短信熬夜的天数
        |                  select user_id,data_date
        |                  from (
        |                           -- 最近一年的数据
        |                           select t1.user_id,call_time, substr(call_time, 1, 10) data_date
        |                           from dwd_fact_emm_behavior_communication_audit t1, dws_emm_behavior_communication_max_data_date t2
        |                           where t1.user_id = t2.user_id and t1.call_time >= date_sub(t2.data_date, 365)
        |                       ) a
        |                       -- 在这个期间就认为是熬夜的
        |                  where  '00' <= substr(call_time,12,2) and substr(call_time,12,2) <= '04'
        |                  group by user_id,data_date
        |                  union all
        |                  -- 微信的熬夜
        |                  select user_id,data_date
        |                  from (
        |                           select chat_audit_id,data_date
        |                           from (
        |                                    -- 微信最近半年最近半年的数据
        |                                    select t1.chat_audit_id, chat_time, substr(chat_time, 1, 10) data_date
        |                                    from dwd_fact_emm_behavior_chat_log t1, dws_emm_behavior_chat_log_max_data_date t2
        |                                    where t1.chat_audit_id = t2.chat_audit_id and t1.chat_time >= date_sub(t2.data_date, 265)
        |                                ) c
        |                                -- 在这个期间就认为是熬夜的
        |                           where  '00' <= substr(chat_time,12,2) and substr(chat_time,12,2) <= '04'
        |                       ) t1 join dwd_dim_emm_behavior_chat_audit t2 on t1.chat_audit_id = t2.chat_audit_id
        |                            join dwd_dim_emm_pub_staff t3 on t2.staff_id = t3.staff_id
        |                  group by user_id,data_date
        |              ) temp
        |         group by  user_id,data_date
        |     ) temp2
        |group by user_id
        |
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
