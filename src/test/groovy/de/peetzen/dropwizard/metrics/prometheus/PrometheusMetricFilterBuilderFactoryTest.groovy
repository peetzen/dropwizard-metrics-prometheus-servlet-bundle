package de.peetzen.dropwizard.metrics.prometheus

import com.codahale.metrics.Metric
import spock.lang.Specification
import spock.lang.Unroll

class PrometheusMetricFilterBuilderFactoryTest extends Specification {

    @Unroll
    def "Filter metric #metric based on regex matches #isAMatch"() {
        given:
        def filter = new PrometheusMetricFilterBuilderFactory().createMetricFilter([".*match"])

        when:
        def matches = filter.matches(metric, Mock(Metric.class))

        then:
        matches == isAMatch

        where:
        metric              || isAMatch
        "this is a match"   || true
        "match this is not" || false
    }

    @Unroll
    def "No filter always matches"() {
        given:
        def filter = new PrometheusMetricFilterBuilderFactory().createMetricFilter()

        when:
        def matches = filter.matches("this is a metric", Mock(Metric.class))

        then:
        matches
    }
}
