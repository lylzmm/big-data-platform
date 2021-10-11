package ods

import ods.ods_template.mysql_to_hive_ods


object ods_emm_behavior_communication_audit_add {

  def main(args: Array[String]): Unit = {
    mysql_to_hive_ods("add", "behavior_communication_audit", "id", "ods_emm_behavior_communication_audit", "create_time")
  }

}
