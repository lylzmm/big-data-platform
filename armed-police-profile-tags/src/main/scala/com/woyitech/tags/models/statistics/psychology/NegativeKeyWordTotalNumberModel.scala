package com.woyitech.tags.models.statistics.psychology

import com.woyitech.tags.models.statistics.TagModel
import com.woyitech.tags.tools.Scope

/**
 *
 * 任务名称：  短信    负面词汇总个数
 * 上游源表：
 * dws_emm_behavior_communication_keyword
 * dws_emm_behavior_communication_max_data_date
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  negative_number
 */

object NegativeKeyWordTotalNumberModel {

  def main(args: Array[String]): Unit = {

    val sql =
      s"""
         |select cast(user_id as string) user_id, cast(count(*)  as string) negative_number
         |from (
         |         select t1.user_id, keyword
         |         from dws_emm_behavior_communication_keyword t1, dws_emm_behavior_communication_max_data_date t2
         |         -- 最近一年的负面关键字的数据
         |         where t1.user_id = t2.user_id and substr(t1.call_time, 1, 10) >= date_sub(t2.data_date, ${Scope.scope})
         |            and length(t1.call_phone) = 11 or instr(t1.call_phone, '+') != 0
         |     ) a lateral view explode(split(keyword, ',')) keyword_temp as word
         |group by user_id
         |""".stripMargin

    TagModel.saveTags(sql)
  }
}
