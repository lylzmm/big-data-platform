package out


import org.apache.spark.sql.SaveMode
import utils.{JdbcUtils, SparkUtils}

import java.util.Properties

object ads_emm_train_ticket {
  def main(args: Array[String]): Unit = {
    val connection = JdbcUtils.getConnection("ahwuj_config.properties", 3304)
    val spark = SparkUtils.createSparkSession(isLocal = true, isHive = true, "ads_emm_train_ticket")

    val pro = new Properties
    pro.put("user", JdbcUtils.getUser)
    pro.put("password", JdbcUtils.getPassword)

    spark.sql("add jar /opt/jar/armed-police-train-ticket-jar-with-dependencies.jar")
    spark.sql("create temporary function TrainTicketUDF as 'udf.TrainTicketUDF'")
    spark.sql(
      """

        |""".stripMargin).write.mode(SaveMode.Append).jdbc(JdbcUtils.getUrl, "emm_message_train_ticket", pro)
    JdbcUtils.close(connection)
  }
}
