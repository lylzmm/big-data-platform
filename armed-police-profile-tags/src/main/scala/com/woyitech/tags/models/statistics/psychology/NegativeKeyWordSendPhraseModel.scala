package com.woyitech.tags.models.statistics.psychology

import com.woyitech.tags.models.statistics.TagModel
import com.woyitech.tags.tools.Scope

/**
 *
 * 任务名称：  短信    发送的负面词组
 * 上游源表：
 * dws_emm_behavior_communication_keyword
 * dws_emm_behavior_communication_max_data_date
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  send_negative_phrase
 */

object NegativeKeyWordSendPhraseModel {

  def main(args: Array[String]): Unit = {

    val sql =
      s"""
         |select cast(user_id as string) user_id, concat_ws('|', collect_set(concat(keyword, '-', ct))) send_negative_phrase
         |from (
         |         select user_id, word as keyword, cast(count(*) as string) ct
         |         from (
         |                  select t1.user_id, call_type, keyword, t1.call_time
         |                  from dws_emm_behavior_communication_keyword t1, dws_emm_behavior_communication_max_data_date t2
         |                       -- 最近一年的负面关键字的数据
         |                  where t1.user_id = t2.user_id and substr(t1.call_time, 1, 10) >= date_sub(t2.data_date, ${Scope.scope})
         |                      and length(t1.call_phone) = 11 or instr(t1.call_phone, '+') != 0
         |              ) a lateral view explode(split(keyword, ',')) keyword_temp as word
         |          -- call_type int comment '短信/彩信类型：1:接收,2:发送，通话：1被叫,2:主叫' ,
         |         where call_type = 2
         |        group by  user_id, word
         |     ) a
         |group by user_id
         |""".stripMargin

    TagModel.saveTags(sql)
  }
}
