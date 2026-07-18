package com.example.jobmatch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.matching")
@Getter @Setter
public class MatchingWeightsConfig {
    private double weightSkill = 0.60;
    private double weightGpa   = 0.15;
    private double weightAuth  = 0.15;
    private double weightMode  = 0.10;
}
