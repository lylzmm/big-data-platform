package com.woyitech.tags.models.statistics.behavior

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  上网审核    最近一年违法访问次数
 * 上游源表：
 * dws_emm_behavior_internet_record_intercept
 * dwd_dim_emm_behavior_internet_audit
 * dwd_dim_emm_pub_staff
 *
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  illegal_visit_number
 */

object IllegalVisitNumberModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id, cast(ct as string) illegal_visit_number
        |from (
        |         select internet_audit_id,count(*) ct
        |         from dws_emm_behavior_internet_record_intercept
        |         group by internet_audit_id
        |) t1 join dwd_dim_emm_behavior_internet_audit t2 on t1.internet_audit_id = t2.id
        |join dwd_dim_emm_pub_staff t3 on t2.staff_id = t3.staff_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
