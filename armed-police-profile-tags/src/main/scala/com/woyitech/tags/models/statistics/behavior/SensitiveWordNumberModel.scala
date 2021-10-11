package com.woyitech.tags.models.statistics.behavior

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  敏感词    最近一年违法词汇次数
 * 上游源表：
 * dwd_fact_emm_behavior_sensitive_word_report
 *
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  sensitive_word_number
 */

object SensitiveWordNumberModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(t1.user_id as string),  cast(count(*) as string) sensitive_word_number
        |from dwd_fact_emm_behavior_sensitive_word_report t1, (
        |    select user_id, max(use_date)  max_date
        |    from dwd_fact_emm_behavior_sensitive_word_report
        |    group by user_id
        |) t2
        |where t1.user_id = t2.user_id and t1.use_date >= date_sub(max_date, 365)
        |group by t1.user_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
