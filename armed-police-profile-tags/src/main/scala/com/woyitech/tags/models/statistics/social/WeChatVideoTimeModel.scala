package com.woyitech.tags.models.statistics.social

import com.woyitech.tags.models.statistics.TagModel


/**
 *
 * 任务名称：  短信    最近一年内视频通话时长
 * 上游源表：
 * dwd_fact_emm_behavior_chat_log
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  wechat_video_time
 */


object WeChatVideoTimeModel {
  def main(args: Array[String]): Unit = {
    val sql =
      """
        |select cast(user_id as string) user_id,cast(wechat_video_time as string) wechat_video_time
        |from (
        |         select chat_audit_id,round(sum(points), 2)  wechat_video_time
        |         from (
        |                   -- 求出每一条视频通话的分钟
        |                  select chat_audit_id, round((points * 60 + seconds) / 60,2 ) points
        |                  from (
        |                           select chat_audit_id, chat_content, split(minutes,':')[0] as points, split(minutes,':')[1] as seconds
        |                           from (
        |                                    -- 一年内的数据集
        |                                    select chat_audit_id, chat_content,split(chat_content,' ')[1] as minutes
        |                                    from (
        |                                             select chat_audit_id,chat_content,data_date,dense_rank() over (partition by chat_audit_id order by data_date desc) rp
        |                                             from (
        |                                                      select chat_audit_id,chat_content, substr(chat_time, 1, 10) data_date
        |                                                      from dwd_fact_emm_behavior_chat_log
        |                                                      where chat_content like '%视频通话:聊天时长%' and substr(chat_time, 1, 10) < substr(current_date, 1, 10)
        |                                                  ) a
        |                                         ) b
        |                                    where rp <= 365
        |                                ) c
        |                       ) d
        |              ) e
        |         group by chat_audit_id
        |) t1
        |join dwd_dim_emm_behavior_chat_audit t2 on t1.chat_audit_id = t2.chat_audit_id
        |join dwd_dim_emm_pub_staff t3 on t2.staff_id = t3.staff_id
        |""".stripMargin
    TagModel.saveTags(sql)
  }
}
