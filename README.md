

# Sample architecture

TODO

# Distributed Trace

* [OpenTracing Transaction without `transaction.type`  JSON](docs/elastic-apm-open-tracing-amazon-sqs-distributed-trace.json)
* [OpenTracing Transaction WITH `transaction.type`  JSON](docs/elastic-apm-open-tracing-amazon-sqs-distributed-trace.json)
   * `co.elastic.apm.opentracing.ElasticApmTags.TYPE.set(tracer.activeSpan(), co.elastic.apm.api.Transaction.TYPE_REQUEST);`

![](https://github.com/cyrille-leclerc/my-elasticapm-custom-instrumentation/raw/opentracing-elastic/docs/images/elastic-apm-open-tracing-custom-transaction-sqs.png)


# References

* [OpenTracing > Best Practices > Tracing Message Bus Scenarios](https://opentracing.io/docs/best-practices/#tracing-message-bus-scenarios)
* [APM Java Agent Reference \[1.x\] » Public API](https://www.elastic.co/guide/en/apm/agent/java/current/public-api.html)
* [AWS > Documentation > AWS SDK for Java > Developer Guide > Sending, Receiving, and Deleting Amazon SQS Messages](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-sqs-messages.html)
