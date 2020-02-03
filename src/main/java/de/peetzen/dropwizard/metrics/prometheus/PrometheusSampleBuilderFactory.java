package de.peetzen.dropwizard.metrics.prometheus;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface PrometheusSampleBuilderFactory extends Discoverable {

    SampleBuilder build();

}