package com.cyrilleleclerc.elastic.apm;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.HeaderExtractor;
import co.elastic.apm.api.Scope;
import co.elastic.apm.api.Transaction;
import co.elastic.apm.attach.ElasticApmAttacher;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.Random;

public class MessageReceiver {

    private final static Random RANDOM = new Random();


    public static void main(String[] args) throws Exception {
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/124526594281/my-queue";

        ElasticApmAttacher.attach(new ElasticConfiguration().getElasticApmConfiguration("message-receiver"));

        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        for (int i = 0; i < 10_000; i++) {
            // retrieve message body and headers (we need "elastic-apm-traceparent" & "traceparent")
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl).withMessageAttributeNames("All");
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();

            // delete messages from the queue
            for (final Message message : messages) {
                SqsUtils.dumpMessage(message);

                Transaction transaction = ElasticApm.startTransactionWithRemoteParent(new AmazonSqsMessageHeaderExtractor(message));
                try (final Scope scopeTx = transaction.activate()) {
                    transaction.setName("MessageReceiver#ProcessMessage");
                    transaction.setType(Transaction.TYPE_REQUEST);
                    // do your thing...

                    sqs.deleteMessage(queueUrl, message.getReceiptHandle());
                    StressTestUtils.incrementProgressBarSuccess();
                } catch (Exception e) {
                    transaction.captureException(e);
                    throw e;
                } finally {
                    transaction.end();
                }
            }
        }
    }

    /**
     * Extract headers from Amazon SQS messages.
     */
    private static class AmazonSqsMessageHeaderExtractor implements HeaderExtractor {
        private final Message message;

        public AmazonSqsMessageHeaderExtractor(Message message) {
            this.message = message;
        }

        @Override
        public String getFirstHeader(String headerName) {
            MessageAttributeValue messageAttributeValue = message.getMessageAttributes().get(headerName);
            return messageAttributeValue == null ? null : messageAttributeValue.getStringValue();
        }
    }
}
