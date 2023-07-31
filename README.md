# Configurable Prometheus Servlet Bundle for Dropwizard

[![CircleCI](https://img.shields.io/circleci/build/gh/peetzen/dropwizard-metrics-prometheus-servlet-bundle)](https://circleci.com/gh/peetzen/dropwizard-metrics-prometheus-servlet-bundle)
[![Maven Central](https://img.shields.io/maven-central/v/de.peetzen.dropwizard/dropwizard-metrics-prometheus-servlet-bundle)](https://search.maven.org/artifact/de.peetzen.dropwizard/dropwizard-metrics-prometheus-servlet-bundle)
[![License](https://img.shields.io/github/license/peetzen/dropwizard-metrics-prometheus-servlet-bundle)](http://www.apache.org/licenses/LICENSE-2.0.html)

[![Dropwizard](https://img.shields.io/badge/dropwizard-v1.x-green)](https://github.com/dropwizard/dropwizard)
[![Dropwizard](https://img.shields.io/badge/dropwizard-v1.3.x-green)](https://github.com/dropwizard/dropwizard)
[![Dropwizard](https://img.shields.io/badge/dropwizard-v2.x-green)](https://github.com/dropwizard/dropwizard)
[![Dropwizard](https://img.shields.io/badge/dropwizard-v3.x-green)](https://github.com/dropwizard/dropwizard)
[![Dropwizard](https://img.shields.io/badge/dropwizard-v4.x-green)](https://github.com/dropwizard/dropwizard)

Adds support for exposing Dropwizard metrics as Prometheus compatible metrics through a dedicated servlet.

Using your Dropwizard applications configuration file it is easy to customize metrics and map metric names to a more user-friendly version including support for labels.

## Documentation

This bundle exposes metrics from a Dropwizard application in a Prometheus compatible format. Through the applications configuration file the name of the exposed metrics can be modified and labels can be added for each metric independently.

Internally the official [Prometheus JVM Client](https://github.com/prometheus/client_java) implementation is being used to map the Dropwizard metrics as well as to expose the metrics through a dedicated servlet accessible via the admin connector
using context path `/prometheusMetrics`.

Information regarding the sanitizing of dropwizard metric names can be found at the [Prometheus DropwizardExports Collector](https://github.com/prometheus/client_java#dropwizardexports-collector) section.

General information regarding the [Prometheus Data Model](https://prometheus.io/docs/concepts/data_model/).

## Getting started

The artifacts including source and binaries are available on the central Maven repositories.

For maven:

```xml

<dependency>
    <groupId>de.peetzen.dropwizard</groupId>
    <artifactId>dropwizard-metrics-prometheus-servlet-bundle</artifactId>
    <version>4.0.2</version>
</dependency>
```

For gradle:

```yaml
implementation group: 'de.peetzen.dropwizard', name: 'dropwizard-metrics-prometheus-servlet-bundle', version: '4.0.2'
```

### Dropwizard compatibility

Due to breaking changes between Dropwizard versions different versions of this library are provided:

| Dropwizard Version | Library Version | Comment                        |
|--------------------|-----------------|--------------------------------|
| `v4.x`             | `4.0.2`         | For latest Dropwizard version. |
| `v3.x`             | `3.0.2`         |                                |
| `v1.x` & `v2.x`    | `1.0.0`         |                                |

### Bundle loading

Your main configuration class needs to implement the _PrometheusMetricsServletBundle_ interface:

```java
public class MyConfiguration extends Configuration implements PrometheusMetricsServletBundleConfiguration {

    // the interface defines a default implementation for getPrometheusMetricsServletConfiguration()
}
```

Add the _PrometheusMetricsServletBundle_ bundle to your application:

```java
public class MyApplication extends Application<MyConfiguration> {

    @Override
    public void initialize(Bootstrap<MyConfiguration> bootstrap) {
        // Add bundle to serv metrics in prometheus compatible format at /prometheusMetrics
        bootstrap.addBundle(new PrometheusMetricsServletBundle());

        super.initialize(bootstrap);
    }

    @Override
    public void run(MyConfiguration configuration, Environment environment) {
        // your existing logic
    }
}
```

### Configuration

In order to expose all metrics from the Dropwizard default metric registry with the default sample mapping under `/prometheusMetrics`, it is enough to include the bundle in your classpath and adjust your application code as shown above.
Optional code and configuration changes allow you to control the behaviour.

#### Built-in configuration options

To change the default behaviour, the suggested approach is through the Dropwizard application configuration file.
You can add a section to your Dropwizard applications configuration file with the following control options:

```yaml
prometheusMetrics:
  path: /prometheusMetrics
  sampleBuilder:
    type: default
    extractDynamicLabels: true
  excludes: (none)
  includes: (all)
  useRegexFilters: false
  useSubstringMatching: false
```

The root key _prometheusMetrics_ depends on the _JsonProperty_ name in your application _Configuration_ implementation and can be changed.

To allow loading the values from the configuration file the main `Configuration` class needs to extend the _PrometheusMetricsServletBundle_ interface and the _getPrometheusMetricsServletConfiguration()_
must be overridden in a way that the actual configuration is loaded from the application configuration file:

```java
public class MyConfiguration extends Configuration implements PrometheusMetricsServletBundleConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private PrometheusMetricsServletConfiguration prometheusMetrics;

    @Override
    public PrometheusMetricsServletConfiguration getPrometheusMetricsServletConfiguration() {
        return prometheusMetrics;
    }
}
```

### Configuration Options

The following general options are available. The sample builder specific options are documented further below.

| Name                               | Default            | Description                                                                                                                   |
|------------------------------------|--------------------|-------------------------------------------------------------------------------------------------------------------------------|
| path                               | /prometheusMetrics | Web context used to expose the metrics in the Prometheus format.                                                              |
| sampleBuilder.type                 | default            | The sample builder type. Provided are `default`, `simple-mapping`, `custom-mapping`. Extendable with your own implementation. |
| sampleBuilder.extractDynamicLabels | true               | Controls if labels should be extracted from the metric name. Available for all built-in types.                                |
| excludes                           | (none)             | Metrics to exclude by name. When defined, matching metrics will not show up.                                                  |
| includes                           | (all)              | Metrics to include by name. When defined, only these metrics will be reported.                                                |
| useRegexFilters                    | false              | Indicates whether the values of the 'includes' and 'excludes' fields should be treated as regular expressions or not.         |
| useSubstringMatching               | false              | Uses a substring matching strategy to determine whether a metric should be processed.                                         |

The inclusion and exclusion rules are defined as:

* If **includes** is empty, then all metrics are included;
* If **includes** is not empty, only metrics from this list are included;
* If **excludes** is empty, no metrics are excluded;
* If **excludes** is not empty, then exclusion rules take precedence over inclusion rules. Thus, if a name matches the exclusion rules it will not be included in reports even if it also matches the inclusion rules.

When neither **useRegexFilters** nor **useSubstringMatching** are enabled, a default exact matching strategy will be used to determine whether a metric should be processed.
In case both **useRegexFilters** and **useSubstringMatching** are set, **useRegexFilters** takes precedence over **useSubstringMatching**.

#### Dynamic Label Sample Builders

By default, dynamic labels are being extracted from the dropwizard metric name. Labels can be encoded in the metric name as _my.metric.something\[label1:value1,label2:value2]_ and are automatically parsed and added to the mapped prometheus metric.
The prometheus metric name becomes _my.metric.something_.

The extraction of dynamic labels can be disabled through the configuration option `sampleBuilder.extractDynamicLabels` for all built-in implementations.

#### Default Sample Builder

Maps Dropwizard metric names to Prometheus names using standard rules.

```yaml
prometheusMetrics:
  sampleBuilder:
    type: default
    extractDynamicLabels: true
```

| Name                 | Default  | Description                                                  |
|----------------------|----------|--------------------------------------------------------------|
| type                 | REQUIRED | The sample builder type. Must be `console`                   |
| extractDynamicLabels | true     | Controls if labels should be extracted from the metric name. |

#### Simple Mapping Sample Builder

Replaces Dropwizard metric names, if they start with a specified string.

```yaml
prometheusMetrics:
  sampleBuilder:
    type: simple-mapping
    extractDynamicLabels: true
    mappings:
      some.metric.one: new.name1
      some.metric.two: new.name2
```

| Name                 | Default  | Description                                                                                                     |
|----------------------|----------|-----------------------------------------------------------------------------------------------------------------|
| type                 | REQUIRED | The sample builder type. Must be `simple-mapping`                                                               | 
| extractDynamicLabels | true     | Controls if labels should be extracted from the metric name.                                                    |
| mappings             | REQUIRED | Explicit mappings defined as _key: value_ pairs. The matched part `key` is replaced using the provided `value`. |

#### Advanced Mapping Sample Builder

Replaces Dropwizard metric names, if they match a specified pattern.

Supports * as a wildcard character to match, and keywords $0, $1, .. to reference the matched value.
The references can be used within the mapped metric name as well as to add dynamic label values.

```yaml
prometheusMetrics:
  sampleBuilder:
    type: custom-mapping
    extractDynamicLabels: true
    mappings:
      some.metric.*: new.name.$0
      other.metric.*:
        name: new.name.$0
        labels:
          - my_label:some_value
          - second_label:other_value
      my.metric.*.*:
        name: new.name.$1
        labels:
          - my_label:$0
```

Mappings can be defined in two formats, a _key: value_ syntax to define the _pattern_ and the _name_ value that should be used instead.
The other option is to provide an object with a _name_ attribute and an optional list of labels, each label expressed as a single string using the format _label:value_.

| Name                 | Default  | Description                                                                                                      |
|----------------------|----------|------------------------------------------------------------------------------------------------------------------|
| type                 | REQUIRED | The sample builder type. Must be `custom-mapping`                                                                |
| extractDynamicLabels | true     | Controls if labels should be extracted from the metric name.                                                     |
| mappings             | REQUIRED | Mapping configurations (both forms are acceptable).                                                              |
| mappings[].name      | REQUIRED | The name value that should be used instead of the matched pattern. Only required if the object notation is used. |
| mappings[].labels    | (none)   | List of labels to add to the exposed Prometheus metric.                                                          |

## Template for Dropwizard's built-in metrics

Dropwizard comes with several instrumented classes by default and those metrics can easily be mapped to a more user-friendly format.

Here is a template for some of the core metrics:

```yaml
prometheusMetrics:
  sampleBuilder:
    type: custom-mapping
    mappings:
      # Dropwizard default instrumented classes
      io.dropwizard.jetty.MutableServletContextHandler.*-requests:
        name: servlet.requests
        labels:
          - method:$0
      io.dropwizard.jetty.MutableServletContextHandler.*-responses:
        name: servlet.responses
        labels:
          - code:$0
      io.dropwizard.jetty.MutableServletContextHandler.percent-*-*:
        name: servlet.responses.percent
        labels:
          - code:$0
          - timeframe:$1
      io.dropwizard.jetty.MutableServletContextHandler.*-*: servlet.$1.$0
      io.dropwizard.jetty.MutableServletContextHandler.*: servlet.$0
      ch.qos.logback.core.Appender.*:
        name: logger
        labels:
          - level:$0
      org.apache.http.client.HttpClient.*.*-requests:
        name: client.http.requests
        labels:
          - name:$0
          - method:$1
      org.apache.http.conn.HttpClientConnectionManager.*.*-connections:
        name: client.http.connections.$1
        labels:
          - name:$0
      org.eclipse.jetty.server.HttpConnectionFactory.*.connections:
        name: server.connections
        labels:
          - port:$0
      org.eclipse.jetty.util.thread.QueuedThreadPool.*.*:
        name: server.threadpools.$1
        labels:
          - name:$0
```

## Metric filtering

The exposed metrics can be limited through the configuration options stated above. The metric name matching happens on the unmapped Dropwizard metrics names.
If more flexibility is required, the `PrometheusMetricsServletConfiguration#getDefaultFilter()` method can be overridden as needed.

In some scenarios you might want to use the exact same filter for the standard [Dropwizard Metric Servlet](https://metrics.dropwizard.io/4.2.0/manual/servlets.html) and this _Prometheus Metric Bundle_.
To support this, the same Servlet Context configuration approach exists. An instance of `MetricFilter` can be provided via the servlet context using the name `de.peetzen.dropwizard.metrics.prometheus.PrometheusMetricsServletBundle.metricFilter`.
If preferred, you can subclass `PrometheusMetricsServletBundle.ContextListener`, which will add a specific `MetricFilter` to the servlet context.

```java
public class MyApplication extends Application<MyConfiguration> {

    @Override
    public void initialize(Bootstrap<MyConfiguration> bootstrap) {
        // add bundle
    }

    @Override
    public void run(MyConfiguration configuration, Environment environment) {
        // programmatically set servlet context attributes
        var filter = MetricFilter.startsWith("org");
        environment.getAdminContext().setAttribute(MetricsServlet.METRIC_FILTER, filter);
        environment.getAdminContext().setAttribute(PrometheusMetricsServletBundle.METRIC_FILTER, filter);
    }
}
```

Furthermore, the internally used Prometheus `Exporter` supports basic filtering.
The filter can be configured through the MetricServlet's initialization parameters. See implementation of the `io.prometheus.client.servlet.common.exporter.Exporter` class for details.
The filtering is done on the mapped metric names.

```java
public class MyApplication extends Application<MyConfiguration> {

    @Override
    public void initialize(Bootstrap<MyConfiguration> bootstrap) {
        // add bundle
    }

    @Override
    public void run(MyConfiguration configuration, Environment environment) {
        // programmatically set init parameters of the internally used metric servlet
        environment.getAdminContext().getServletHandler()
            .getServlet(PrometheusMetricsServletBundle.DEFAULT_SERVLET_NAME)
            .setInitParameter("name-must-be-equal-to", "org_eclipse_jetty_util_thread_QueuedThreadPool_dw_admin_jobs");
    }
}
```

## Extension Support

The supported functionality is not enough, you want a more customized mapping solution or a different configuration structure?

Do not hesitate to extend _PrometheusSampleBuilderFactory_ and provide your own custom implementation.

## Development notes

- The configuration options for metric filtering and the implementation are based on the [Dropwizard Metrics Reporters](https://www.dropwizard.io/en/latest/manual/configuration.html#all-reporters).
- The used `io.prometheus.client.dropwizard.DropwizardExports` implementation does not support filtering of metric attributes.
- Due to an [open issue](https://github.com/prometheus/client_java/issues/518) with the Prometheus Client a few classes have been copied over and altered to allow more flexible match patterns.