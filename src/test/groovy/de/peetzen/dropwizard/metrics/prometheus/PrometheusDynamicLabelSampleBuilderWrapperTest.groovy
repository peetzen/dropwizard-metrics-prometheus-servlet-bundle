package de.peetzen.dropwizard.metrics.prometheus

import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class PrometheusDynamicLabelSampleBuilderWrapperTest extends Specification {

    def delegate = Mock(SampleBuilder)

    @Subject
    def builder = new PrometheusDynamicLabelSampleBuilderWrapper(delegate)

    @Unroll
    def "createSample for #dropwizardName"() {
        given:
        def nameSuffix = "random"
        def value = 0.1

        when:
        builder.createSample(dropwizardName, nameSuffix, lbNames, lbValues, value)

        then:
        1 * delegate.createSample(expectedName, nameSuffix, expectedLabelNames, expectedLabelValues, value)

        where:
        dropwizardName                              | lbNames   | lbValues   || expectedName | expectedLabelNames   | expectedLabelValues
        "my.metric"                                 | []        | []         || "my.metric"  | []                   | []
        "my.metric"                                 | ["label"] | ["value"]  || "my.metric"  | ["label"]            | ["value"]
        "my.metric[]"                               | []        | []         || "my.metric"  | []                   | []
        "my.metric[label:value]"                    | []        | []         || "my.metric"  | ["label"]            | ["value"]
        "my.metric[second:value2]"                  | ["first"] | ["value1"] || "my.metric"  | ["first", "second"]  | ["value1", "value2"]
        "my.metric[label1:value,label2:value]"      | []        | []         || "my.metric"  | ["label1", "label2"] | ["value", "value"]
        "my.metric[unsupported_format]"             | []        | []         || "my.metric"  | []                   | []
        "my.metric[label:value,unsupported_format]" | []        | []         || "my.metric"  | ["label"]            | ["value"]
    }
}
