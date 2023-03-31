package com.suyeq.yarn.distributed;

import com.sun.jndi.toolkit.url.Uri;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.URL;
import org.apache.hadoop.yarn.api.records.UpdatedContainer;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.client.api.async.NMClientAsync;
import org.apache.hadoop.yarn.client.api.async.impl.NMClientAsyncImpl;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : denglinhai
 * @date : 11:49 2023/3/31
 * 执行分布式的ApplicationMaster
 */
public class DistributedAM {
    private final static Logger logger = LoggerFactory.getLogger(DistributedAM.class);

    // 充当锁
    private Object lock = new Object();
    // 任务个数
    private int childTaskNum = 2;
    // 已完成任务个数
    private int childTaskCompletedNum = 0;

    private NMClientAsyncImpl nmClientAsync;

    public static void main(String[] args) {
        DistributedAM master = new DistributedAM();
        master.run();
    }

    /**
     * AppMaster 运行
     */
    public void run() {
        try {
            Configuration conf = new Configuration();
            // rm回调处理器
            AMRMClientAsync.AbstractCallbackHandler rmCallBackHandler = new RMCallBackHandler();
            // 开启am-rm client，建立rm-am的通道，用于注册AM, allocListener负责处理AM的响应
            AMRMClientAsync<AMRMClient.ContainerRequest> amRmClient = AMRMClientAsync.createAMRMClientAsync(1000, rmCallBackHandler);;
            amRmClient.init(conf);
            amRmClient.start();
            String hostName = NetUtils.getHostname();
            logger.info("host name is [{}]", hostName);
            // 注册至RM
            amRmClient.registerApplicationMaster(hostName, -1, null);
            // 运行程序
            // 初始化nmClient
            nmClientAsync = new NMClientAsyncImpl(new NMCallBackHandler());
            nmClientAsync.init(conf);
            nmClientAsync.start();
            // 运行程序
            doRun(amRmClient);
            // 解除注册
            amRmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "SUCCESS", null);
            // am-rm客户端关闭
            amRmClient.stop();
            // nm客户端关闭
            nmClientAsync.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实际运行程序，就一个输出
     */
    private void doRun(AMRMClientAsync<AMRMClient.ContainerRequest> amRmClient) throws Exception {
        // 申请两个资源容器
        for (int i = 0; i < childTaskNum; i++) {
            // 向rm申请一个1M内存,1个CPU的资源容器
            int memory = 1024;
            int vCores = 1;
            AMRMClient.ContainerRequest containerRequest = new AMRMClient.ContainerRequest(
                    Resource.newInstance(memory, vCores), null, null, Priority.UNDEFINED);
            amRmClient.addContainerRequest(containerRequest);
        }
        synchronized (lock) {
            // 等待子任务完成
            lock.wait();
        }
        System.out.println("HELLO WORLD");
    }

    /**
     * RM的异步回调
     */
    public class RMCallBackHandler extends AMRMClientAsync.AbstractCallbackHandler {

        @Override
        public void onContainersCompleted(List<ContainerStatus> list) {
            for (ContainerStatus status : list) {
                synchronized (lock) {
                    System.out.println(++childTaskCompletedNum + " container completed");
                    // 子任务全部完成
                    if (childTaskCompletedNum == childTaskNum) {
                        lock.notify();
                    }
                }
            }
        }

        @Override
        public void onContainersAllocated(List<Container> list) {
            try {
                for (Container container : list) {
                    Configuration conf = new Configuration();
                    System.out.println("container allocated, Node=" + container.getNodeHttpAddress());
                    // 构建AM<->NM客户端并开启
                    // 还是YarnClient containerLaunchContext那一套，这把直接去HDFS系统取文件，因为和YarnClient打包到一个jar上传
                    Map<String, LocalResource> localResources = new HashMap<String, LocalResource>() {{
                        //NameNode的ip和端口
                        String hdfsJarPath = "/lib/bigdata-1.0.0-jar-with-dependencies.jar";
                        String jarName = "bigdata-1.0.0-jar-with-dependencies.jar";
                        FileSystem fs = FileSystem.get(new URI("hdfs://192.168.183.128:9000"), conf, "bigdata");
                        FileStatus fileStatus = fs.getFileStatus(new Path(hdfsJarPath));
                        put(jarName, LocalResource.newInstance(
                                URL.fromURI(new URI(hdfsJarPath)),
                                LocalResourceType.FILE, LocalResourceVisibility.APPLICATION,
                                fileStatus.getLen(), fileStatus.getModificationTime()));
                    }};
                    Map<String, String> env = new HashMap<>();
                    StringBuilder classPathEnv = new StringBuilder(ApplicationConstants.Environment.CLASSPATH.$$())
                            .append(ApplicationConstants.CLASS_PATH_SEPARATOR).append("./*");
                    for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH, YarnConfiguration.DEFAULT_YARN_CROSS_PLATFORM_APPLICATION_CLASSPATH)) {
                        classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR);
                        classPathEnv.append(c.trim());
                    }
                    env.put("CLASSPATH", classPathEnv.toString());
                    List<String> commands = new ArrayList<String>() {{
                        // 传入ip地址作为参数
                        add(ApplicationConstants.Environment.JAVA_HOME.$$() + "/bin/java -Xmx200m com.suyeq.yarn.distributed.ChildTask");
                    }};
                    ContainerLaunchContext containerLaunchContext = ContainerLaunchContext.newInstance(
                            localResources, env, commands, null, null, null);
                    // nm节点启动container
                    nmClientAsync.startContainerAsync(container, containerLaunchContext);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onContainersUpdated(List<UpdatedContainer> list) {

        }

        @Override
        public void onShutdownRequest() {

        }

        @Override
        public void onNodesUpdated(List<NodeReport> list) {

        }

        @Override
        public float getProgress() {
            return 0;
        }

        @Override
        public void onError(Throwable throwable) {

        }
    }

    /**
     * nodeManager的异步回调
     */
    private class NMCallBackHandler extends NMClientAsync.AbstractCallbackHandler {

        @Override
        public void onContainerStarted(ContainerId containerId, Map<String, ByteBuffer> map) {

        }

        @Override
        public void onContainerStatusReceived(ContainerId containerId, ContainerStatus containerStatus) {

        }

        @Override
        public void onContainerStopped(ContainerId containerId) {

        }

        @Override
        public void onStartContainerError(ContainerId containerId, Throwable throwable) {

        }

        @Override
        public void onContainerResourceIncreased(ContainerId containerId, Resource resource) {

        }

        @Override
        public void onContainerResourceUpdated(ContainerId containerId, Resource resource) {

        }

        @Override
        public void onGetContainerStatusError(ContainerId containerId, Throwable throwable) {

        }

        @Override
        public void onIncreaseContainerResourceError(ContainerId containerId, Throwable throwable) {

        }

        @Override
        public void onUpdateContainerResourceError(ContainerId containerId, Throwable throwable) {

        }

        @Override
        public void onStopContainerError(ContainerId containerId, Throwable throwable) {

        }
    }

}
