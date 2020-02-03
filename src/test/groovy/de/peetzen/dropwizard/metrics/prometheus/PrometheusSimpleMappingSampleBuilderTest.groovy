package de.peetzen.dropwizard.metrics.prometheus

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class PrometheusSimpleMappingSampleBuilderTest extends Specification {

    @Subject
    def builder

    @Unroll
    def "createSample"() {
        given:
        def mappings = [mapping("my.metric.something", "new.name"),
                        mapping("my.metric.else", "new.name")]
        builder = new PrometheusSimpleMappingSampleBuilder(mappings)

        when:
        def sample = builder.createSample(dropwizardName, null, [], [], 0.1)

        then:
        sample.name == expected

        where:
        dropwizardName                 || expected
        "my.metric.does.not.match"     || "my_metric_does_not_match"
        "my.metric.something"          || "new_name"
        "my.metric.something.specific" || "new_name_specific"
        "my.metric.else"               || "new_name"
    }

    private PrometheusSimpleMappingSampleBuilder.MapperConfig mapping(from, to) {
        new PrometheusSimpleMappingSampleBuilder.MapperConfig(from, to)
    }
}
