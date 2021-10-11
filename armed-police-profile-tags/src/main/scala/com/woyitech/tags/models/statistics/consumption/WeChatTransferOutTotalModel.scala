package com.woyitech.tags.models.statistics.consumption

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  微信   转出总额
 * 上游源表：
 * dws_emm_behavior_chat_log_keyword
 * dws_emm_behavior_chat_log_max_data_date
 * 计算周期：  最近一个月
 * 更新周期：  1天
 * 字段模型：  wechat_transfer_out_total
 * 字段说明：  content_type  352表示转账 272表示红包  |    chat_status  收到：0  发出：1
 */
object WeChatTransferOutTotalModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id, cast(money as string) wechat_transfer_out_total
        |from (
        |         select t1.chat_audit_id, round(sum(money), 2) money
        |         from dwd_fact_emm_behavior_chat_log t1, dws_emm_behavior_chat_log_max_data_date t2
        |         -- chat_status 1 发
        |         where content_type = 352 and chat_status = 1 and t1.chat_audit_id = t2.chat_audit_id and substr(t1.chat_time, 1, 10) >= date_sub(t2.data_date, 30)
        |         group by t1.chat_audit_id
        |) t1 join dwd_dim_emm_behavior_chat_audit t2 on t1.chat_audit_id = t2.chat_audit_id
        |join dwd_dim_emm_pub_staff t3 on t2.staff_id = t3.staff_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
