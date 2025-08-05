package com.team12.searchservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.team12.searchservice.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUris.replace("http://", ""))
                .withConnectTimeout(Duration.ofSeconds(30))
                .withSocketTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public HealthIndicator elasticsearchHealthIndicator() {
        return new HealthIndicator() {
            @Override
            public Health health() {
                try {
                    return Health.up()
                            .withDetail("message", "Elasticsearch connection configured")
                            .withDetail("cluster", "smr-elasticsearch")
                            .build();
                } catch (Exception e) {
                    return Health.down()
                            .withDetail("message", "Cannot connect to Elasticsearch")
                            .withDetail("error", e.getMessage())
                            .build();
                }
            }
        };
    }
} 