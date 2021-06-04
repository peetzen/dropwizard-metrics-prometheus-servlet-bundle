package de.peetzen.dropwizard.metrics.prometheus;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class PrometheusMetricsServletConfiguration {

    @NotNull
    @JsonProperty
    private String path = "/prometheusMetrics";

    @Valid
    @NotNull
    @JsonProperty
    private PrometheusSampleBuilderFactory sampleBuilder = new PrometheusDefaultSampleBuilderFactory();

    @JsonProperty
    private List<String> includes = new ArrayList<>();

    public CollectorRegistry createCollectorRegistry(MetricRegistry registry) {
        // create CollectorRegistry instance instead of reusing CollectorRegistry.defaultRegistry to make sure we
        // are not conflicting with other use cases
        CollectorRegistry collectorRegistry = new CollectorRegistry(true);
        collectorRegistry.register(new DropwizardExports(registry, new PrometheusMetricFilterBuilderFactory().createMetricFilter(includes), sampleBuilder.build()));
        return collectorRegistry;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
