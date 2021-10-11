package ads

import org.apache.spark.sql.SaveMode
import utils.SparkUtils

object ads_emm_message_nature_group {

  def main(args: Array[String]): Unit = {

    val savaTableName = "ads_emm_message_nature_group"

    val session = SparkUtils.createSparkSession(isLocal = false, isHive = true, savaTableName)
    val sql =
      """
        |select uuid()                          id
        |     , msgIds
        |     , groupCode
        |     , loginName
        |     , staff_name
        |     , msgOriginName
        |     , msgNature
        |     , cast(cashNum as decimal(10, 2)) cashNum
        |     , yearMonth
        |from (
        |         select CONCAT_WS(",", collect_set(id)) msgIds
        |              , groupCode
        |              , loginName
        |              , staff_name
        |              , msgOriginName
        |              , msgNature
        |              , sum(money)                      cashNum
        |              , yearMonth
        |         from (
        |                  select cast(t1.id as string)       id
        |                       , t3.groupCode
        |                       , t2.login_name               loginName
        |                       , t2.staff_name
        |                       , t1.msgOriginName
        |                       , t1.msgNature
        |                       , t1.money
        |                       , substr(t1.call_time, 1, 10) yearMonth
        |                  from dws_emm_message_nature_group_duplicate t1,
        |                       dwd_dim_emm_pub_staff t2,
        |                       dwd_dim_emm_user t3
        |                  where t1.user_id = t2.user_id
        |                    and money is not null
        |                    and t2.login_name is not null
        |                    and t2.login_name = t3.loginName
        |                    and t1.money != 0
        |                    and t1.money != ""
        |                    and t1.msgOriginName != ''
        |              ) a
        |         group by groupCode
        |                , loginName
        |                , staff_name
        |                , msgOriginName
        |                , msgNature
        |                , yearMonth
        |     ) b
        |""".stripMargin

    session.sql(sql).coalesce(1).write.mode(SaveMode.Overwrite).insertInto(savaTableName)

    session.close()
  }
}
