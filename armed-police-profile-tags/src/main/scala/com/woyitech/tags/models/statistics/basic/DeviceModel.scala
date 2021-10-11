package com.woyitech.tags.models.statistics.basic

import com.woyitech.tags.models.statistics.TagModel

/**
 *
 * 任务名称：  用户    用户的设备个数
 * 上游源表：
 * dwd_dim_emm_mdm_device
 * dwd_dim_emm_pub_staff
 *
 * 计算周期：
 * 更新周期：  1天
 * 字段模型：  device_number
 */

object DeviceModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string) user_id,cast(count(*) as string) device_number
        |from dwd_dim_emm_mdm_device t1, dwd_dim_emm_pub_staff t2
        |where t1.staff_id = t2.staff_id and device_status != 7
        |group by user_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
