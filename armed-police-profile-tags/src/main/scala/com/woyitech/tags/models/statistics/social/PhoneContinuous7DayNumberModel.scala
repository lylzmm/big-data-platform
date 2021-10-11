package com.woyitech.tags.models.statistics.social

import com.woyitech.tags.models.statistics.TagModel


/**
 *
 * create external table if not exists default.dws_emm_behavior_communication_day_message_number(
 * user_id bigint comment '用户ID',
 * call_name string comment '联系人名称' ,
 * call_phone string comment '联系人电话' ,
 * data_date string comment '时间',
 * `number` int comment '个数'
 * )
 * stored as parquet
 * location  '/user/hive/warehouse/dws/dws_emm_behavior_communication_day_message_number'
 * tblproperties("orc.compress"="SNAPPY");
 *
 *
 *
 * insert overwrite table dws_emm_behavior_communication_day_message_number
 * select user_id, call_name, call_phone, data_date,count(*)
 * from (
 * select user_id, call_name, if(instr(call_phone, '+') !=0, substr(call_phone , 4, length(call_phone)), call_phone) call_phone, substr(call_time, 1, 10) data_date
 * from dwd_fact_emm_behavior_communication_audit
 * ) a
 * where length(call_phone) = 11
 * group by user_id, call_name, call_phone, data_date;
 *
 *
 *
 * create external table if not exists default.dws_emm_behavior_communication_max_data_date(
 * user_id bigint comment '用户ID' ,
 * data_date string comment '最大时间'
 * )
 * stored as parquet
 * location  '/user/hive/warehouse/dws/dws_emm_behavior_communication_max_data_date'
 * tblproperties("orc.compress"="SNAPPY");
 *
 *
 * insert overwrite table dws_emm_behavior_communication_max_data_date
 * select user_id, if(data_date > current_date, current_date, data_date) data_date
 * from (
 * select user_id, substr(max(call_time), 1, 10) data_date
 * from dwd_fact_emm_behavior_communication_audit
 * group by user_id
 * ) a;
 */

/**
 *
 * 任务名称：  短信    连续7天以上聊天人个数
 * 上游源表：
 * dws_emm_behavior_communication_day_message_number
 * dws_emm_behavior_communication_max_data_date
 * 计算周期：  每个用户近一年内数据
 * 更新周期：  1天
 * 字段模型：  phone_continuous_7_day_number
 */

object PhoneContinuous7DayNumberModel {

  def main(args: Array[String]): Unit = {

    val sql =
      """
        |select cast(user_id as string), cast(count(*) as string) phone_continuous_7_day_number
        |from (
        |         select user_id, call_phone, sum(ct)
        |         from  (
        |                   select user_id, call_phone, sub_date, count(*) ct
        |                   from (
        |                            select user_id, call_phone, date_sub(data_date, rp) sub_date
        |                            from (
        |                                     select t1.user_id, call_phone, t1.data_date, rank() over (partition by t1.user_id, call_phone order by t1.data_date) rp
        |                                     from dws_emm_behavior_communication_day_message_number t1, dws_emm_behavior_communication_max_data_date t2
        |                                     -- 每个用户最近一年的数据
        |                                     where t1.user_id = t2.user_id and t1.data_date >= date_sub(t2.data_date, 365)
        |                                 ) a
        |                        ) b
        |                   group by user_id, call_phone, sub_date
        |                   having count(*) >= 7
        |               ) c
        |         group by user_id, call_phone
        |     ) d
        |group by user_id
        |""".stripMargin

    TagModel.saveTags(sql)
  }
}
