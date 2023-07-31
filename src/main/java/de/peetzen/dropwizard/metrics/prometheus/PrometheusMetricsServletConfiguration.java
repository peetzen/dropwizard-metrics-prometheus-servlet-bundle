package de.peetzen.dropwizard.metrics.prometheus;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.metrics.common.BaseReporterFactory;
import io.prometheus.client.dropwizard.DropwizardExports;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.Set;

public class PrometheusMetricsServletConfiguration {

    /**
     * Reusing the BaseReporterFactory to benefit from the standard MetricFilter builder
     * functionality. At the same, directly time extending it is problematic as this Prometheus Servlet
     * is not a "classic" reporter that is working with a specific report cadence.
     * Delegating to a dummy implementation seems to be the best option and allows to reuse the otherwise not
     * accessible StringMatchingStrategy implementations.
     */
    @JsonIgnore
    private final BaseReporterFactory filterFactory = new BaseReporterFactory() {
        @Override
        public ScheduledReporter build(MetricRegistry registry) {
            return null;
        }
    };

    @NotEmpty
    @JsonProperty
    private String path = "/prometheusMetrics";

    @Valid
    @NotNull
    @JsonProperty
    private PrometheusSampleBuilderFactory sampleBuilder = new PrometheusDefaultSampleBuilderFactory();

    @JsonProperty
    public Set<String> getIncludes() {
        return filterFactory.getIncludes();
    }

    @JsonProperty
    public void setIncludes(Set<String> includes) {
        filterFactory.setIncludes(includes);
    }

    @JsonProperty
    public Set<String> getExcludes() {
        return filterFactory.getExcludes();
    }

    @JsonProperty
    public void setExcludes(Set<String> excludes) {
        filterFactory.setExcludes(excludes);
    }

    @JsonProperty
    public boolean getUseRegexFilters() {
        return filterFactory.getUseRegexFilters();
    }

    @JsonProperty
    public void setUseRegexFilters(boolean useRegexFilters) {
        filterFactory.setUseRegexFilters(useRegexFilters);
    }

    @JsonProperty
    public boolean getUseSubstringMatching() {
        return filterFactory.getUseSubstringMatching();
    }

    @JsonProperty
    public void setUseSubstringMatching(boolean useSubstringMatching) {
        filterFactory.setUseSubstringMatching(useSubstringMatching);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public PrometheusSampleBuilderFactory getSampleBuilder() {
        return sampleBuilder;
    }

    public void setSampleBuilder(PrometheusSampleBuilderFactory sampleBuilder) {
        this.sampleBuilder = sampleBuilder;
    }

    protected MetricFilter getDefaultMetricFilter() {
        return filterFactory.getFilter();
    }

    protected DropwizardExports createDropwizardExports(MetricRegistry registry, MetricFilter filter) {
        Objects.requireNonNull(registry);

        // if no filter was provided e.g. through the ServletContext, use the logic of this configuration class
        if (filter == null) {
            filter = getDefaultMetricFilter();
        }

        return new DropwizardExports(registry, filter, sampleBuilder.build());
    }

}
