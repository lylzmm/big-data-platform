package dws

import org.apache.spark.sql.SaveMode
import utils.SparkUtils

object dws_emm_message_nature_group_duplicate {
  def main(args: Array[String]): Unit = {
    val savaTableName = "dws_emm_message_nature_group_duplicate"
    val session = SparkUtils.createSparkSession(isLocal = false, isHive = true, "dws_emm_message_nature_group_duplicate")

    val sql =
      """
        |select max(id),
        |       user_id,
        |       max(device_id),
        |       max(call_name),
        |       max(call_phone),
        |       max(call_time),
        |       max(sms_content),
        |       max(msgOriginName),
        |       max(msgNature),
        |       money
        |from (
        |         select max(id) id,
        |                user_id,
        |                max(device_id) device_id,
        |                month,
        |                max(call_name)  call_name,
        |                max(call_phone) call_phone,
        |                max(call_time)  call_time,
        |                max(sms_content) sms_content,
        |                max(msgOriginName) msgOriginName,
        |                max(msgNature) msgNature,
        |                money,
        |                max(due_date)   due_date
        |         from (
        |                  select *, substr(call_time, 1, 7) month
        |                  from dws_emm_message_nature_group
        |                  where (msgNature = '4'
        |                     or msgNature = '7'
        |                     or msgNature = '8')
        |
        |              ) t1
        |         group by user_id, month, money
        |        order by  call_time
        |     ) t2
        |where due_date != ''
        |group by user_id, due_date, money
        |union all
        |select id,
        |       user_id,
        |       device_id,
        |       call_name,
        |       call_phone,
        |       call_time,
        |       sms_content,
        |       msgOriginName,
        |       msgNature,
        |       money
        |from dws_emm_message_nature_group
        |where msgNature != '4'
        |  and msgNature != '7'
        |  and msgNature != '8';
        |""".stripMargin

    session.sql(sql).coalesce(1).write.mode(SaveMode.Overwrite).insertInto(savaTableName)

    session.close()
  }
}
