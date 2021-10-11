package utils;


import org.apache.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {
    private static final Logger logger = Logger.getLogger(String.valueOf(PropertyUtils.class));

    private static final Properties properties;

    static {
        properties = new Properties();
        InputStream inputStream = PropertyUtils.class.getClassLoader().getResourceAsStream("application.properties");
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                logger.error("读取配置文件失败", e);
            }
        }
    }

    //供外部调用，配置文件只读取一次，节约内存
    public static String getProperty(String param) {
        return properties.getProperty(param);
    }
}

