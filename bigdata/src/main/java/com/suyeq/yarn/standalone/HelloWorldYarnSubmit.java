package com.suyeq.yarn.standalone;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.URL;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : denglinhai
 * @date : 11:57 2023/3/31
 * 提交appMater到yarn上运行
 */
public class HelloWorldYarnSubmit {
    private final static Logger logger = LoggerFactory.getLogger(HelloWorldYarnSubmit.class);

    public static void main(String[] args) {
        HelloWorldYarnSubmit client = new HelloWorldYarnSubmit();
        try {
            client.run();
        } catch (Exception e) {
            logger.error("client run exception , please check log file.", e);
        }
    }

    /**
     * 客户端提交
     */
    public void run() throws Exception {
        /**=====1.配置=====**/
        Configuration conf = new Configuration();
        // 设置rm所在的ip地址
        conf.set("yarn.resourcemanager.hostname", "192.168.183.128");
        /**=====2.申请app=====**/
        // 创建YarnClient和ResourceManager进行交互
        YarnClient yarnClient = YarnClient.createYarnClient();
        // 初始配置
        yarnClient.init(conf);
        // 开启(建立连接)
        yarnClient.start();
        // 向RM发送请求创建应用
        YarnClientApplication application = yarnClient.createApplication();
        // 准备应用提交上下文(RM要求你提交的信息格式)
        ApplicationSubmissionContext applicationSubmissionContext = application.getApplicationSubmissionContext();
        // 获取分配的应用id
        ApplicationId appId = applicationSubmissionContext.getApplicationId();
        logger.info("appId: {}", appId);
        /**=====3.设置应用名称=====**/
        // 设置应用名称
        applicationSubmissionContext.setApplicationName("Hello World");
        /**=====4.准备程序(jar包)=====**/
        String hdfsJarPath = "/lib/bigdata-1.0.0-jar-with-dependencies.jar";
        String jarName = "bigdata-1.0.0-jar-with-dependencies.jar";

        //获取jar包的信息
        Configuration configuration = new Configuration();
        FileSystem fs  = FileSystem.get(new URI("hdfs://192.168.183.128:9000"), configuration, "bigdata");
        FileStatus fsStatus = fs.getFileStatus(new Path(hdfsJarPath));
        LocalResource scRsrc = LocalResource.newInstance(
                URL.fromURI(new URI(hdfsJarPath)),
                LocalResourceType.FILE, LocalResourceVisibility.APPLICATION,
                fsStatus.getLen(), fsStatus.getModificationTime());
        Map<String, LocalResource> localResources = new HashMap<String, LocalResource>() {{
            put(jarName, scRsrc);
        }};
        /**=====5.准备程序环境=====**/
        Map<String, String> env = new HashMap<>();
        // 任务的运行依赖jar包的准备
        StringBuilder classPathEnv = new StringBuilder(ApplicationConstants.Environment.CLASSPATH.$$())
                .append(ApplicationConstants.CLASS_PATH_SEPARATOR).append("./*");
        // yarn依赖包
        for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH, YarnConfiguration.DEFAULT_YARN_CROSS_PLATFORM_APPLICATION_CLASSPATH)) {
            classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR);
            classPathEnv.append(c.trim());
        }
        env.put("CLASSPATH", classPathEnv.toString());
        logger.info("classpath is [{}]", classPathEnv.toString());

        /**=====6.准备启动命令=====**/
        List<String> commands = new ArrayList<String>() {{
            String standAlone = "com.suyeq.yarn.standalone.HelloWorldAM";
            String distributed = "com.suyeq.yarn.distributed.DistributedAM";
            add(ApplicationConstants.Environment.JAVA_HOME.$$() + "/bin/java -Xmx300m " + distributed);
        }};

        /**=====7.构造am container运行资源+环境+脚本=====**/
        ContainerLaunchContext amContainer = ContainerLaunchContext.newInstance(
                localResources, env, commands, null, null, null);
        // 准备am Container的运行环境
        applicationSubmissionContext.setAMContainerSpec(amContainer);
        /**=====8.设置am程序所需资源=====**/
        int memory = 1024;
        int vCores = 2;
        applicationSubmissionContext.setResource(Resource.newInstance(memory, vCores));
        /**=====9.提交并开始作业=====**/
        yarnClient.submitApplication(applicationSubmissionContext);
        /**=====10.查询作业是否完成=====**/
        for (;;) {
            Thread.sleep(500);
            ApplicationReport applicationReport = yarnClient.getApplicationReport(appId);
            YarnApplicationState state = applicationReport.getYarnApplicationState();
            FinalApplicationStatus status = applicationReport.getFinalApplicationStatus();
            if (state.equals(YarnApplicationState.FINISHED)) {
                if (status.equals(FinalApplicationStatus.SUCCEEDED)) {
                    logger.info("程序运行成功!");
                    break;
                } else  {
                    logger.error("程序运行失败!");
                    break;
                }
            } else if (state.equals(YarnApplicationState.FAILED) || state.equals(YarnApplicationState.KILLED) ) {
                logger.error("程序运行失败!");
                break;
            }
            logger.info("计算中...");
        }
    }
}
