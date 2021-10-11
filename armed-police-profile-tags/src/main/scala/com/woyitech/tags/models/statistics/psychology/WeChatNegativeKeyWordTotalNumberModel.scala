package com.woyitech.tags.models.statistics.psychology

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  微信负面词汇    总个数
 * 上游源表：
 * dws_emm_behavior_chat_log_keyword
 * dws_emm_behavior_chat_log_max_data_date
 *
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  wechat_negative_number
 */

object WeChatNegativeKeyWordTotalNumberModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id, cast(count(*) as string) wechat_negative_number
        |from (
        |         select user_id, word as keyword
        |         from (
        |                  -- 求出每个用户最近一年的数据，负面关键按照,分割了
        |                  -- 例如：害怕,睡不着
        |                  select user_id, keyword
        |                  from dws_emm_behavior_chat_log_keyword t1, dws_emm_behavior_chat_log_max_data_date t2
        |                  where t1.chat_audit_id = t2.chat_audit_id and substr(t1.chat_time, 1, 10) >= date_sub(t2.data_date, 365)
        |              ) a lateral view explode(split(keyword, ',')) keyword_temp as word
        |) b
        |where user_id is not null
        |group by user_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
