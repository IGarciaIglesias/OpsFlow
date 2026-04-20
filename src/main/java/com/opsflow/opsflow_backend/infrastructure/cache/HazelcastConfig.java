package com.opsflow.opsflow_backend.infrastructure.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    @Bean
    public Config hazelcastConfiguration() {
        Config config = new Config();
        config.setInstanceName("opsflow-hazelcast");

        config.addMapConfig(
                new MapConfig()
                        .setName("dashboardSummary")
                        .setTimeToLiveSeconds(30)
                        .setEvictionConfig(new EvictionConfig()
                                .setEvictionPolicy(EvictionPolicy.LRU)
                                .setMaxSizePolicy(MaxSizePolicy.FREE_HEAP_SIZE)
                                .setSize(100))
        );

        return config;
    }
}