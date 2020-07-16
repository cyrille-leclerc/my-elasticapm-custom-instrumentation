package com.cyrilleleclerc.elastic.apm;


import co.elastic.apm.attach.ElasticApmAttacher;
import co.elastic.apm.opentracing.ElasticApmTracer;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;

import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class MessageSender {

    private final static Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/124526594281/my-queue";

        ElasticApmAttacher.attach(new ElasticConfiguration().getElasticApmConfiguration("message-sender"));

        Tracer tracer = new ElasticApmTracer();

        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

        for (int i = 0; i < 10_000; i++) {
            Span span = tracer.buildSpan("MessageSender#send").start();
            try (Scope scope = tracer.scopeManager().activate(span)) {
                SendMessageRequest sendMessageRequest = new SendMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withMessageBody("hello world")
                        .withDelaySeconds(5);

                Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CLIENT);
                Tags.MESSAGE_BUS_DESTINATION.set(tracer.activeSpan(), queueUrl);

                tracer.inject(span.context(), Format.Builtin.TEXT_MAP, new AmazonSqsMessageCarrier(sendMessageRequest));

                SqsUtils.dumpMessage(sendMessageRequest);
                sqs.sendMessage(sendMessageRequest);
            } catch (Exception e) {
                Tags.ERROR.set(span, true);
                throw e;
            } finally {
                span.finish();
            }

            StressTestUtils.incrementProgressBarSuccess();

            Thread.sleep(RANDOM.nextInt(2_000));
        }
    }

    public static class AmazonSqsMessageCarrier implements TextMap {
        private final SendMessageRequest messageRequest;

        AmazonSqsMessageCarrier(SendMessageRequest messageRequest) {
            this.messageRequest = messageRequest;
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            throw new UnsupportedOperationException("carrier is write-only");
        }

        @Override
        public void put(String key, String value) {
            messageRequest.addMessageAttributesEntry(key, new MessageAttributeValue().withDataType("String").withStringValue(value));
        }
    }

}
