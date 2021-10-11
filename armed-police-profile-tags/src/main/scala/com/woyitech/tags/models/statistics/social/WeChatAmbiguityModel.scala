package com.woyitech.tags.models.statistics.social

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  微信    微信有暧昧的个数
 * 上游源表：
 *            dwd_fact_emm_behavior_chat_log
 *            dws_emm_behavior_chat_log_max_data_date
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  ambiguity_number
 */

object WeChatAmbiguityModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
