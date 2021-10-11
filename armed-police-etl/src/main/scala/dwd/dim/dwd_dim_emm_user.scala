package dwd.dim

import dwd.dwd_template


object dwd_dim_emm_user {

  def main(args: Array[String]): Unit = {
    dwd_template.odsToDwd("dim","","ods_emm_user","dwd_dim_emm_user")
  }

}
