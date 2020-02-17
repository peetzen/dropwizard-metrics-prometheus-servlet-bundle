package de.peetzen.dropwizard.metrics.prometheus;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public abstract class PrometheusDynamicLabelSampleBuilderFactory implements PrometheusSampleBuilderFactory {

    @Valid
    @NotNull
    @JsonProperty
    private boolean extractDynamicLabels = true;

    @Override
    public SampleBuilder build() {
        SampleBuilder builder = createSampleBuilder();
        return extractDynamicLabels ? new PrometheusDynamicLabelSampleBuilderWrapper(builder) : builder;
    }

    protected abstract SampleBuilder createSampleBuilder();

    public boolean isExtractDynamicLabels() {
        return extractDynamicLabels;
    }

    public void setExtractDynamicLabels(boolean extractDynamicLabels) {
        this.extractDynamicLabels = extractDynamicLabels;
    }
}