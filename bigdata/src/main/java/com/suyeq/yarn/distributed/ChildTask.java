package com.suyeq.yarn.distributed;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.net.NetUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author : denglinhai
 * @date : 16:39 2023/3/31
 */
public class ChildTask {

    public static void main(String[] args) throws Exception {
        //获取文件系统
        Configuration configuration = new Configuration();
        //NameNode的ip和端口
        FileSystem fs  = FileSystem.get(new URI("hdfs://192.168.183.128:9000"), configuration, "bigdata");
        // hostName
        String hostName = NetUtils.getHostname() + System.currentTimeMillis();
        // 创建一个文件夹
        fs.mkdirs(new Path("/lib/child/" + hostName));
        fs.close();
    }

}
