package de.peetzen.dropwizard.metrics.prometheus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Supports replacing the first part of a dropwizard metric name with something more user friendly.
 */
@JsonTypeName("simple-mapping")
public class PrometheusSimpleMappingSampleBuilderFactory extends PrometheusDynamicLabelSampleBuilderFactory {

    @Valid
    @NotNull
    @JsonProperty
    private Map<String, String> mappings;

    @Override
    protected SampleBuilder createSampleBuilder() {
        List<PrometheusSimpleMappingSampleBuilder.MapperConfig> mapperConfigs = mappings.entrySet().stream()
            .map(c -> new PrometheusSimpleMappingSampleBuilder.MapperConfig(c.getKey(), c.getValue()))
            .collect(Collectors.toList());
        return new PrometheusSimpleMappingSampleBuilder(mapperConfigs);
    }

}