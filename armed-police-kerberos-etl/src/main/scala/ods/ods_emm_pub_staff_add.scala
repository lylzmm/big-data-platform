package ods

import ods.ods_template.mysql_to_hive_ods
import utils.KerberosUtils

object ods_emm_pub_staff_add {

  def main(args: Array[String]): Unit = {


    mysql_to_hive_ods("all", "pub_staff", "staff_id", "ods_emm_pub_staff", "");
  }

}
