package com.woyitech.tags.models.statistics.basic

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  短信    微信账号个数
 * 上游源表：
 * dwd_dim_emm_behavior_chat_audit
 * dwd_dim_emm_pub_staff
 *
 * 计算周期：
 * 更新周期：  1天
 * 字段模型：  wechat_number
 */

object WeChatModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id, cast(count(*) as string) wechat_number
        |from dwd_dim_emm_behavior_chat_audit t1,dwd_dim_emm_pub_staff t2
        |where t1.staff_id = t2.staff_id and t1.app_pkg_name= 'com.tencent.mm'
        |group by user_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
