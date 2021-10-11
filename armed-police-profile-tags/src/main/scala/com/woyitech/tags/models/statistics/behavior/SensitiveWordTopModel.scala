package com.woyitech.tags.models.statistics.behavior

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  敏感词    最近一年违法词汇词组top5
 * 上游源表：
 * dwd_fact_emm_behavior_sensitive_word_report
 *
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  sensitive_word_top5
 */

object SensitiveWordTopModel {

  def main(args: Array[String]): Unit = {
    val sql =
      """
        |select cast(user_id as string) user_id, concat_ws('|', collect_set(concat(sensitive_name, '-', ct))) sensitive_word_top5
        |from (
        |         select user_id,sensitive_name,ct
        |         from (
        |                  select user_id,sensitive_name,ct,row_number() over (partition by user_id order by ct desc) rp
        |                  from (
        |                           -- 最近一年内的铭感词
        |                           select t1.user_id, sensitive_name, count(*) ct
        |                           from dwd_fact_emm_behavior_sensitive_word_report t1, (
        |                               select user_id, max(use_date)  max_date
        |                               from dwd_fact_emm_behavior_sensitive_word_report
        |                               group by user_id
        |                           ) t2
        |                           where t1.user_id = t2.user_id and t1.use_date >= date_sub(max_date, 365)
        |                           group by t1.user_id, sensitive_name
        |                       ) c
        |              ) d
        |         where rp <=5
        |     ) a
        |group by user_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
