package test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import utils.KerberosUtils;

public class Test {
    public static void main(String[] args) throws Exception {


        KerberosUtils.loginUserFromKeytab("local");


        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        System.out.println(fs);

        Path path = new Path("hdfs://node03:8020/user");
        if (fs.exists(path)) {
            System.out.println("===contains===");
        }
        RemoteIterator<LocatedFileStatus> list = fs.listFiles(path, true);
        while (list.hasNext()) {
            LocatedFileStatus fileStatus = list.next();
            System.out.println(fileStatus.getPath());
        }
        // 3 关闭资源
        fs.close();
    }
}
