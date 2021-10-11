package com.woyitech.tags.models.statistics.basic

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  短信    房贷
 * 上游源表：
 * dwd_fact_emm_behavior_communication_audit
 *
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  house_loan_money
 */

object HouseLoanModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id, cast(money as string) house_loan_money
        |from (
        |         select user_id, money, rank() over (partition by user_id order by call_time desc) rp
        |         from (
        |                  select user_id,
        |                         call_time,
        |                         sms_content,
        |                         split(MessageUDF(sms_content), '->')[2] money
        |                  from dwd_fact_emm_behavior_communication_audit
        |                  where call_type = 1
        |                    and msg_type = 2
        |                    and call_phone != '10000'
        |                    and call_phone != '10010'
        |                    and call_phone != '10086'
        |                    and sms_content like '%房贷%'
        |              ) a
        |         where money is not null
        |     ) a
        |where rp = 1
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
