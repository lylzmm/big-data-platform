package com.woyitech.tags.meta

case class HBaseMeta(
                      zkHosts: String,
                      zkPort: String,
                      hbaseTable: String,
                      family: String,
                      selectFieldNames: String
                    )


object HBaseMeta{
  def getHBaseMeta(ruleMap: Map[String, String]): HBaseMeta = {
    // TODO: 实际开发中，应该先判断各个字段是否有值，没有值直接给出提示，终止程序
    HBaseMeta(
      ruleMap("zkHosts"),
      ruleMap("zkPort"),
      ruleMap("hbaseTable"),
      ruleMap("family"),
      ruleMap("selectFieldNames")
    )
  }
}
