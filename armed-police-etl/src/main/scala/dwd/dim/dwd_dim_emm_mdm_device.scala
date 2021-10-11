package dwd.dim

import dwd.dwd_template

object dwd_dim_emm_mdm_device {
  def main(args: Array[String]): Unit = {
    dwd_template.odsToDwd("dim","","ods_emm_mdm_device","dwd_dim_emm_mdm_device")
  }
}
