package com.woyitech.tags.models.statistics.psychology

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  微信    拒绝电话及视频次数
 * 上游源表：
 * dwd_fact_emm_behavior_chat_log
 * dws_emm_behavior_chat_log_max_data_date
 * dwd_dim_emm_behavior_chat_audit
 * dwd_dim_emm_pub_staff
 *
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  wechat_refused_to_number
 *
 */

object WechatRefusedTodModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id,  cast(ct as string)  wechat_refused_to_number
        |from (
        |        select chat_audit_id, count(*) ct
        |         from (
        |             select t1.chat_audit_id
        |             from dwd_fact_emm_behavior_chat_log t1, dws_emm_behavior_chat_log_max_data_date t2
        |             where chat_content like '%:已拒绝%' and t1.chat_audit_id = t2.chat_audit_id and substr(t1.chat_time, 1, 10) >= date_sub(t2.data_date, 365)
        |             ) a
        |         group by chat_audit_id
        |     ) t1
        |join dwd_dim_emm_behavior_chat_audit t2 on t1.chat_audit_id = t2.chat_audit_id
        |join dwd_dim_emm_pub_staff t3 on t2.staff_id = t3.staff_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
