package de.peetzen.dropwizard.metrics.prometheus;

public interface PrometheusMetricsServletBundleConfiguration {

    default PrometheusMetricsServletConfiguration getPrometheusMetricsServletConfiguration() {
        return new PrometheusMetricsServletConfiguration();
    }
}
