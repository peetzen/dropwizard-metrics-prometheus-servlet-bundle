package de.peetzen.dropwizard.metrics.prometheus;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PrometheusMetricsServletBundle implements ConfiguredBundle<PrometheusMetricsServletBundleConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsServletBundle.class);

    private static final String DEFAULT_SERVLET_NAME = "prometheusMetrics";

    private final String servletName;

    public PrometheusMetricsServletBundle() {
        servletName = DEFAULT_SERVLET_NAME;
    }

    public PrometheusMetricsServletBundle(String servletName) {
        Objects.requireNonNull(servletName, "servlet name missing");
        this.servletName = servletName;
    }

    @Override
    public void run(PrometheusMetricsServletBundleConfiguration bundleConfig, Environment env) throws Exception {
        PrometheusMetricsServletConfiguration config = bundleConfig.getPrometheusMetricsServletConfiguration();
        CollectorRegistry collectorRegistry = config.createCollectorRegistry(env.metrics());
        MetricsServlet servlet = new MetricsServlet(collectorRegistry);

        logger.info("Registering MetricsServlet with name: {} for path {}", servletName, config.getPath());
        env.admin() // use admin port
            .addServlet(servletName, servlet)
            .addMapping(config.getPath());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // do nothing. Definition required for Dropwizard 1.x
    }
}
