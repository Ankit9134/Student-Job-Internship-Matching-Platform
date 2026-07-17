package com.example.jobmatch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.matching")
@Getter @Setter
public class MatchingWeightsConfig {
    /** Relative weight of skill overlap in the final score. Defaults sum to 1.0. */
    private double weightSkill = 0.6;
    private double weightGpa = 0.2;
    private double weightAuth = 0.2;
}
