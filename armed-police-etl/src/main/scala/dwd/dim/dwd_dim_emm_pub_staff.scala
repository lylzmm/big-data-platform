package dwd.dim

import dwd.dwd_template

object dwd_dim_emm_pub_staff {
  def main(args: Array[String]): Unit = {
    dwd_template.odsToDwd("dim","","ods_emm_pub_staff","dwd_dim_emm_pub_staff")
  }
}
