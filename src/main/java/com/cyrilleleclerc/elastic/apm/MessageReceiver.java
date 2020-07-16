package com.cyrilleleclerc.elastic.apm;


import co.elastic.apm.api.Transaction;
import co.elastic.apm.attach.ElasticApmAttacher;
import co.elastic.apm.opentracing.ElasticApmTracer;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;

import java.util.*;

public class MessageReceiver {

    private final static Random RANDOM = new Random();


    public static void main(String[] args) throws Exception {
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/124526594281/my-queue";

        ElasticApmAttacher.attach(new ElasticConfiguration().getElasticApmConfiguration("message-receiver"));

        Tracer tracer = new ElasticApmTracer();

        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        for (int i = 0; i < 10_000; i++) {
            // retrieve message body and headers (we need "elastic-apm-traceparent", "traceparent" and "tracestate" at least)
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl).withMessageAttributeNames("All");
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();

            // delete messages from the queue
            for (final Message message : messages) {
                SqsUtils.dumpMessage(message);

                SpanContext parentSpanContext = tracer.extract(Format.Builtin.TEXT_MAP, new AmazonSqsMessageExtractAdapter(message));
                if (parentSpanContext == null) {
                    System.out.println("WARNING no parent context found");
                }
                Span span = tracer.buildSpan("MessageReceiver#ProcessMessage").asChildOf(parentSpanContext).start();
                try (Scope scope = tracer.scopeManager().activate(span)) {

                    // do your thing...

                    sqs.deleteMessage(queueUrl, message.getReceiptHandle());
                    StressTestUtils.incrementProgressBarSuccess();
                } catch (Exception e) {
                    Tags.ERROR.set(span, true);
                    throw e;
                } finally {
                    span.finish();
                }
            }
        }
    }

    /**
     * Extract headers from Amazon SQS messages.
     */
    public static class AmazonSqsMessageExtractAdapter implements TextMap {

        final Message message;

        AmazonSqsMessageExtractAdapter(Message message) {
            this.message = message;
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<String, MessageAttributeValue> entry: message.getMessageAttributes().entrySet()) {
                result.put(entry.getKey(), entry.getValue().getStringValue());
            }

            return result.entrySet().iterator();
        }

        @Override
        public void put(String key, String value) {
            throw new UnsupportedOperationException("carrier is read-only");
        }
    }
}
