package com.cyrilleleclerc.elastic.apm;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Scope;
import co.elastic.apm.api.Span;
import co.elastic.apm.api.Transaction;
import co.elastic.apm.attach.ElasticApmAttacher;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.Random;

public class MessageSender {

    private final static Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/124526594281/my-queue";
        String queueName = queueUrl.substring(queueUrl.lastIndexOf('/') + 1);

        ElasticApmAttacher.attach(new ElasticConfiguration().getElasticApmConfiguration("message-sender"));

        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

        for (int i = 0; i < 10_000; i++) {
            Transaction transaction = ElasticApm.startTransaction();
            try (final Scope scopeTx = transaction.activate()) {
                transaction.setName("MessageSender#send");
                transaction.setType(Transaction.TYPE_REQUEST);

                SendMessageRequest sendMessageRequest = new SendMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withMessageBody("hello world")
                        .withDelaySeconds(5);

                //
                Span span = ElasticApm.currentSpan().
                        startSpan("external", "sqs", null).
                        setName("send" + " " + queueName);

                try (final Scope scopeSpan = transaction.activate()) {
                    span.injectTraceHeaders((headerName, headerValue) -> sendMessageRequest.addMessageAttributesEntry(headerName, new MessageAttributeValue().withDataType("String").withStringValue(headerValue)));

                    SqsUtils.dumpMessage(sendMessageRequest);
                    sqs.sendMessage(sendMessageRequest);
                } catch (Exception e) {
                    span.captureException(e);
                    throw e;
                } finally {
                    span.end();
                }

                StressTestUtils.incrementProgressBarSuccess();
            } catch (Exception e) {
                transaction.captureException(e);
                throw e;
            } finally {
                transaction.end();
            }
            Thread.sleep(RANDOM.nextInt(5_000));
        }
    }
}
