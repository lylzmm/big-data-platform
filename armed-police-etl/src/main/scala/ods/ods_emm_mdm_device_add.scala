package ods

import ods.ods_template.mysql_to_hive_ods

object ods_emm_mdm_device_add {

  def main(args: Array[String]): Unit = {
    mysql_to_hive_ods("all", "mdm_device", "device_id", "ods_emm_mdm_device", "")
  }
}
