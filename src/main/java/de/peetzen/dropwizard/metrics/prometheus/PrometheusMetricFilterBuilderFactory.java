package de.peetzen.dropwizard.metrics.prometheus;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrometheusMetricFilterBuilderFactory {


    public static MetricFilter createMetricFilter(List<String> includes) {
        return new PrometheusMetricFilter(includes);
    }

    private static class PrometheusMetricFilter implements MetricFilter {

        List<String> includes;

        protected PrometheusMetricFilter(List<String> includes) {
            this.includes = includes;
        }

        @Override
        public boolean matches(String name, Metric metric) {
            if (includes == null) {
                return true;
            }
            boolean isIncluded = includes.stream().anyMatch(regex -> {
                Pattern compile = Pattern.compile(regex);
                Matcher matcher = compile.matcher(name);
                return matcher.matches();
            });
            return includes.isEmpty() || isIncluded;
        }
    }
}
