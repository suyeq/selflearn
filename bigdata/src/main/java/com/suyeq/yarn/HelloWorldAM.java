package com.suyeq.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : denglinhai
 * @date : 11:49 2023/3/31
 * 打印hello，world的ApplicationMaster
 */
public class HelloWorldAM {
    private final static Logger logger = LoggerFactory.getLogger(HelloWorldAM.class);

    public static void main(String[] args) {
        HelloWorldAM master = new HelloWorldAM();
        master.run();
    }

    /**
     * AppMaster 运行
     */
    public void run() {
        try {
            // 开启am-rm client，建立rm-am的通道，用于注册AM
            AMRMClientAsync amRmClient = AMRMClientAsync.createAMRMClientAsync(1000, null);
            amRmClient.init(new Configuration());
            amRmClient.start();
            String hostName = NetUtils.getHostname();
            logger.info("host name is [{}]", hostName);
            // 注册至RM
            amRmClient.registerApplicationMaster(hostName, -1, null);
            // 运行程序
            doRun();
            // 解除注册
            amRmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "SUCCESS", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实际运行程序，就一个输出
     */
    private void doRun() {
        System.out.println("HELLO WORLD");
    }

}
