package com.ichanskiy.softserve.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private Integer port;

    @Value("${elasticsearch.protocol}")
    private String protocol;

    @Value("${elasticsearch.connect.timeout}")
    private Integer connectTimeout;

    @Value("${elasticsearch.socket.timeout}")
    private Integer socketTimeout;

    @Bean
    @Primary
    public RestHighLevelClient elasticsearchClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, protocol))
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout(connectTimeout)
                                .setSocketTimeout(socketTimeout)));
    }

}
