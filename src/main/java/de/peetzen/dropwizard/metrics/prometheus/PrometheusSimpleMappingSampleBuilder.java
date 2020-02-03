package de.peetzen.dropwizard.metrics.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

import java.util.List;

public class PrometheusSimpleMappingSampleBuilder implements SampleBuilder {

    public static class MapperConfig {
        private final String match;
        private final String replacement;

        public MapperConfig(String match, String replacement) {
            this.match = match;
            this.replacement = replacement;
        }
    }

    private final DefaultSampleBuilder defaultMetricSampleBuilder = new DefaultSampleBuilder();
    private final List<MapperConfig> mappings;

    public PrometheusSimpleMappingSampleBuilder(List<MapperConfig> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            throw new IllegalArgumentException("A mapping configuration is required");
        }
        this.mappings = mappings;
    }

    @Override
    public Collector.MetricFamilySamples.Sample createSample(String dropwizardName, String nameSuffix, List<String> additionalLabelNames, List<String> additionalLabelValues, double value) {
        String metricName = getReplacedMetricName(dropwizardName);

        return defaultMetricSampleBuilder.createSample(
            metricName, nameSuffix,
            additionalLabelNames,
            additionalLabelValues,
            value
        );
    }

    private String getReplacedMetricName(String dropwizardName) {
        return mappings.stream()
            .filter(v -> dropwizardName.startsWith(v.match))
            .map(v -> v.replacement + dropwizardName.substring(v.match.length()))
            .findFirst()
            .orElse(dropwizardName);
    }
}
