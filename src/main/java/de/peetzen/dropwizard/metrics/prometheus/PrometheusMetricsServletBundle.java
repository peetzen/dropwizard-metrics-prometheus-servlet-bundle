package de.peetzen.dropwizard.metrics.prometheus;

import com.codahale.metrics.MetricFilter;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Environment;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PrometheusMetricsServletBundle implements ConfiguredBundle<PrometheusMetricsServletBundleConfiguration> {
    public static final String METRIC_FILTER = PrometheusMetricsServletBundle.class.getCanonicalName() + ".metricFilter";
    public static final String DEFAULT_SERVLET_NAME = "prometheusMetrics";

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsServletBundle.class);

    private final CollectorRegistry collectorRegistry = new CollectorRegistry(false);
    private final String servletName;

    public PrometheusMetricsServletBundle() {
        servletName = DEFAULT_SERVLET_NAME;
    }

    public PrometheusMetricsServletBundle(String servletName) {
        Objects.requireNonNull(servletName, "servlet name missing");
        this.servletName = servletName;
    }

    public String getServletName() {
        return servletName;
    }

    @Override
    public void run(PrometheusMetricsServletBundleConfiguration bundleConfig, Environment env) {
        var bundleServletConfig = bundleConfig.getPrometheusMetricsServletConfiguration();

        logger.info("Registering MetricsServlet with name: {} for path {}", servletName, bundleServletConfig.getPath());
        env.admin() // use admin port
            .addServlet(servletName, new MetricsServlet(collectorRegistry) {
                @Override
                public void init(ServletConfig servletConfig) throws ServletException {
                    // Allows programmatically setting ServletContext attributes
                    // and e.g. provide a Metric Filter implementation in the Application#run(..) implementation.
                    var metricFilter = (MetricFilter) servletConfig.getServletContext().getAttribute(METRIC_FILTER);
                    var collector = bundleServletConfig.createDropwizardExports(env.metrics(), metricFilter);
                    collectorRegistry.register(collector);

                    super.init(servletConfig);
                }
            })
            .addMapping(bundleServletConfig.getPath());
    }

    public static abstract class ContextListener implements ServletContextListener {

        /**
         * Returns the {@link MetricFilter} that shall be used to filter metrics.
         *
         * @return a metric filter instance
         */
        protected abstract MetricFilter getMetricFilter();

        @Override
        public void contextInitialized(ServletContextEvent event) {
            var context = event.getServletContext();
            context.setAttribute(METRIC_FILTER, getMetricFilter());
        }
    }
}
