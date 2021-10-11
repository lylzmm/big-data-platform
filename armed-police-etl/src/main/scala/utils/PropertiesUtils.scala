package utils

import java.io.InputStream
import java.util.Properties

object PropertiesUtils {
  //单例配置文件
  lazy val getProperties: Properties = {
    val properties = new Properties()
    val in: InputStream = this.getClass.getClassLoader.getResourceAsStream("application.properties");
    properties.load(in)
    properties
  }
}