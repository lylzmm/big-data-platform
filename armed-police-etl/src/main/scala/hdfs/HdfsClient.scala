package hdfs

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.junit.Test

import java.io.IOException
import java.net.URI

object HdfsClient {

  @Test
  @throws[IOException]
  @throws[InterruptedException]
  def testHdfsClient(): Unit = { //1. 创建HDFS客户端对象,传入uri， configuration , user
    val fs = FileSystem.get(URI.create("hdfs://node01:9820"), new Configuration, "hadoop")
    val pathName = "/user/hive/warehouse/dws/dws_emm_behavior_communication_user"
    val listStatus = fs.listStatus(new Path(pathName))
    for (fileStatus <- listStatus) { // 如果是文件
      if (fileStatus.isFile) System.out.println("f:" + fileStatus.getPath.getName)
      else {
        val path = fileStatus.getPath.getName
        var count = 0
        val list = fs.listStatus(new Path(pathName + "/" + path))
        for (l <- list) {
          if (l.isFile) count += 1
        }
        System.out.println(path + "===" + count)
      }
    }
    //3. 关闭资源
    fs.close()
  }

}
