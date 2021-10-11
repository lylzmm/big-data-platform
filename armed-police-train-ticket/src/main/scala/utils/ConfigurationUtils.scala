package utils

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FileSystem, Path}

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI

object ConfigurationUtils {

  def readSql(fileName: String) = {
    System.setProperty("HADOOP_USER_NAME", "hadoop")
    val fileSystem: FileSystem = FileSystem.get(new URI("hdfs://node01:9820"), new Configuration, "hadoop")
    val fsDataInputStream: FSDataInputStream = fileSystem.open(new Path("hdfs://node01:9820/spark-sql/" + fileName))
    val buf: BufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

    val sql = new StringBuilder
    var line = buf.readLine
    while (line != null) {
      sql.append(line.replace(";", "")).append("\n")
      line = buf.readLine
    }
    sql
  }
}
