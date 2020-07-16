package com.cyrilleleclerc.elastic.apm;

import java.util.HashMap;
import java.util.Map;

public class ElasticConfiguration {

    public Map<String, String> getElasticApmConfiguration(String serviceName) {
        Map<String, String> configuration = new HashMap<>();

        //  see https://www.elastic.co/guide/en/apm/agent/java/current/config-stacktrace.html
        configuration.put("service_name", serviceName);
        configuration.put("service_version", "1.0.0-SNAPSHOT");
        configuration.put("applicationPackages", "com.cyrilleleclerc.elastic.apm");
        //  ENVIRONMENT SPECIFIC PARAMETERS
        //  https://www.elastic.co/guide/en/apm/agent/java/current/config-reporter.html#config-server-urls
        configuration.put("server_urls", "http://localhost:8200");
        // https://www.elastic.co/guide/en/apm/agent/java/current/config-reporter.html#config-secret-token
        configuration.put("secret_token", "my_secret_token");
        // https://www.elastic.co/guide/en/apm/agent/java/current/config-core.html#config-environment
        configuration.put("environment", "dev");



        // https://www.elastic.co/guide/en/apm/agent/java/current/config-core.html#config-disable-instrumentations
        // everything except 'opentracing'
        configuration.put("disable_instrumentations", "annotations, apache-commons-exec, apache-httpclient, asynchttpclient, concurrent, dubbo, elasticsearch-restclient, exception-handler, executor, experimental, grails, grpc, hibernate-search, http-client, jax-rs, jax-ws, jdbc, jedis, jms, jsf, kafka, lettuce, log4j, logging, mongodb-client, mule, okhttp, process, public-api, quartz, redis, redisson, render, scheduled, servlet-api, servlet-api-async, servlet-input-stream, slf4j, spring-mvc, spring-resttemplate, spring-service-name, spring-view-render, ssl-context, urlconnection");

        return configuration;
    }
}
