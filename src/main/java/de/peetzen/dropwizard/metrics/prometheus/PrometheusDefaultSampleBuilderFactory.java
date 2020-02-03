package de.peetzen.dropwizard.metrics.prometheus;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

/**
 * Forwards dropwizard metrics without any specific mapping. The usual character sanitizing applies.
 */
@JsonTypeName("default")
public class PrometheusDefaultSampleBuilderFactory extends PrometheusDynamicLabelSampleBuilderFactory {

    @Override
    protected SampleBuilder createSampleBuilder() {
        return new DefaultSampleBuilder();
    }

}