package dwd.fact

import dwd.dwd_template

object dwd_fact_emm_behavior_sensitive_word_report {
  def main(args: Array[String]): Unit = {
    dwd_template.odsToDwd("dim", "", "ods_emm_behavior_sensitive_word_report", "dwd_fact_emm_behavior_sensitive_word_report")
  }
}
