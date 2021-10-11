package utils

import utils.HiveUtil


object HiveUtilTest {

  def main(args: Array[String]): Unit = {
//    ods_template.getStartId("dwd_fact_emm_behavior_communication_audit","id")
    HiveUtil.getMaxPartitionMinId("dwd_fact_emm_behavior_communication_audit","id")
  }

}
