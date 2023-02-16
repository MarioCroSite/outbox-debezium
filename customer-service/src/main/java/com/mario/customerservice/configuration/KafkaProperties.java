package com.mario.customerservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.mario.kafka")
public class KafkaProperties {
    private String bootstrapServer;
    private String securityProtocol;
    private String trustedPackages;
    private String consumerGroupId;

    public String getBootstrapServer() {
        return bootstrapServer;
    }

    public void setBootstrapServer(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getTrustedPackages() {
        return trustedPackages;
    }

    public void setTrustedPackages(String trustedPackages) {
        this.trustedPackages = trustedPackages;
    }

    public String getConsumerGroupId() {
        return consumerGroupId;
    }

    public void setConsumerGroupId(String consumerGroupId) {
        this.consumerGroupId = consumerGroupId;
    }

    @Override
    public String toString() {
        return "KafkaProperties{" +
                "bootstrapServer='" + bootstrapServer + '\'' +
                ", securityProtocol='" + securityProtocol + '\'' +
                ", trustedPackages='" + trustedPackages + '\'' +
                ", consumerGroupId='" + consumerGroupId + '\'' +
                '}';
    }
}
