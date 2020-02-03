package de.peetzen.dropwizard.metrics.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support for decoding labels from the dropwizard metric name.
 * <p>
 * Labels can be encoded as [label1:value,label2:value] at the end of the metric name.
 */
public class PrometheusDynamicLabelSampleBuilderWrapper implements SampleBuilder {

    // Documentation on allowed patterns: https://prometheus.io/docs/concepts/data_model/#metric-names-and-labels
    private static final Pattern PATTERN_METRIC_NAME = Pattern.compile("([\\w\\.-]+)\\[([\\w\\W]*)\\]");
    private static final Pattern PATTERN_LABEL = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*):([^,]+)");

    private final SampleBuilder delegate;

    public PrometheusDynamicLabelSampleBuilderWrapper(SampleBuilder delegate) {
        Objects.requireNonNull(delegate, "delegate missing");
        this.delegate = delegate;
    }

    @Override
    public Collector.MetricFamilySamples.Sample createSample(String dropwizardName, String nameSuffix, List<String> additionalLabelNames, List<String> additionalLabelValues, double value) {

        // check if metric name contains dynamic labels
        Matcher matcher = PATTERN_METRIC_NAME.matcher(dropwizardName);
        if (!matcher.find()) {
            // no dynamic labels
            return delegate.createSample(dropwizardName, nameSuffix, additionalLabelNames, additionalLabelValues, value);
        }

        String metricName = matcher.group(1);
        String labels = matcher.group(2);

        // decode labels in the format of <key>:<value>,<key>:<value>
        List<String> labelNames = new ArrayList<>(additionalLabelNames);
        List<String> labelValues = new ArrayList<>(additionalLabelValues);
        Matcher labelMatcher = PATTERN_LABEL.matcher(labels);
        while (labelMatcher.find()) {
            labelNames.add(labelMatcher.group(1));
            labelValues.add(labelMatcher.group(2).trim());
        }

        return delegate.createSample(metricName, nameSuffix, labelNames, labelValues, value);

    }
}
