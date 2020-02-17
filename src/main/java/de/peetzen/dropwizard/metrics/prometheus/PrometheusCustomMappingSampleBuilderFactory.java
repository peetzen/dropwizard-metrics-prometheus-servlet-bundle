package de.peetzen.dropwizard.metrics.prometheus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.peetzen.dropwizard.metrics.prometheus.repackaged.CustomMappingSampleBuilder;
import de.peetzen.dropwizard.metrics.prometheus.repackaged.MapperConfig;
import io.dropwizard.jackson.Jackson;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Supports matching metric names based on a match pattern that includes * as a wildcard character. The matched parts
 * can be used to define a custom name as well as custom labels.
 */
@JsonTypeName("custom-mapping")
public class PrometheusCustomMappingSampleBuilderFactory extends PrometheusDynamicLabelSampleBuilderFactory {

    public static class MetricMapperConfig {
        @Valid
        @NotNull
        @JsonProperty
        private String name;

        @JsonProperty
        private List<String> labels;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }
    }

    @Valid
    @NotNull
    @JsonProperty
    private Map<String, JsonNode> mappings;

    @Override
    protected SampleBuilder createSampleBuilder() {
        List<MapperConfig> mapperConfigs = mappings.entrySet().stream()
            .map(e -> parseMapping(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
        return new CustomMappingSampleBuilder(mapperConfigs);
    }

    public Map<String, JsonNode> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, JsonNode> mappings) {
        this.mappings = mappings;
    }

    private MapperConfig parseMapping(String key, JsonNode jsonNode) {
        if (jsonNode.isTextual() || jsonNode.isBoolean()) {
            // Mapping to a name without any other configuration
            return new MapperConfig(key, toTemplateFormat(jsonNode.asText()), Collections.emptyMap());
        } else if (jsonNode.isObject()) {
            // Advanced configuration using MetricMapperConfig object
            final MetricMapperConfig config;
            try {
                config = Jackson.newObjectMapper().treeToValue(jsonNode, MetricMapperConfig.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Wrong format for mapping configuration '" + key + "'", e);
            }
            return new MapperConfig(key, toTemplateFormat(config.name), mapLabels(config.labels));
        } else {
            throw new IllegalArgumentException("Unsupported format for mapping configuration '" + key + "'");
        }
    }

    private static Map<String, String> mapLabels(List<String> labels) {
        if (labels == null) {
            return Collections.emptyMap();
        }

        return labels.stream().collect(
            Collectors.toMap(
                s -> s.split("\\:")[0],
                s -> toTemplateFormat(s.split("\\:")[1])));
    }

    private static String toTemplateFormat(String input) {
        // replacing the simplified group reference pattern with the expected format
        // e.g. , my.example.$1.$2.total -> my.example.${1}.${2}.total
        return input.replaceAll("\\$(\\d+)", "\\${$1}");
    }

}