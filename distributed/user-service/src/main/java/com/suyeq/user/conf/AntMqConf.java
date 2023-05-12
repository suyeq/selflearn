package com.suyeq.user.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author : denglinhai
 * @date : 15:30 2023/5/12
 */
@Component
@ConfigurationProperties(prefix = "mq")
public class AntMqConf {
    private String server;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
