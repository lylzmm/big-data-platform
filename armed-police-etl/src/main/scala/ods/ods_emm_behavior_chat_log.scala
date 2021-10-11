package ods

import ods.ods_template.mysql_to_hive_ods

object ods_emm_behavior_chat_log {
  def main(args: Array[String]): Unit = {
    mysql_to_hive_ods("add", "behavior_chat_log", "behavior_chat_log_id", "ods_emm_behavior_chat_log", "create_time")
  }
}
