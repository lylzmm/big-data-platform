package ods

import ods.ods_template.mysql_to_hive_ods

object ods_emm_behavior_sensitive_word_report_add {

  def main(args: Array[String]): Unit = {
    mysql_to_hive_ods("all", "behavior_sensitive_word_report", "id", "ods_emm_behavior_sensitive_word_report", "")
  }
}
