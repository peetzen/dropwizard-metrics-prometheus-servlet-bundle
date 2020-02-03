package de.peetzen.dropwizard.metrics.prometheus;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PrometheusMetricsServletConfiguration {

    @NotNull
    @JsonProperty
    private String path = "/prometheusMetrics";

    @Valid
    @NotNull
    @JsonProperty
    private PrometheusSampleBuilderFactory sampleBuilder;

    public String getPath() {
        return path;
    }

    public CollectorRegistry createCollectorRegistry(MetricRegistry registry) {
        CollectorRegistry collectorRegistry = new CollectorRegistry(false); // same as defaut but has its own instance
        collectorRegistry.register(new DropwizardExports(registry, sampleBuilder.build()));
        return collectorRegistry;
    }
}
