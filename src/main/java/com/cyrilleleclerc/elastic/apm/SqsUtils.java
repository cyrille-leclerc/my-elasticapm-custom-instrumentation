package com.cyrilleleclerc.elastic.apm;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.stream.Collectors;

public class SqsUtils {

    static boolean debug = false;

    public static void  dumpMessage(Message message) {
        if (!debug) return;
        System.out.println("Message: " +
                message.getMessageAttributes().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue().getStringValue()).collect(Collectors.joining(", "))
        );
    }

    public static void dumpMessage(SendMessageRequest sendMessageRequest) {
        if (!debug) return;

        System.out.println("SendMessageRequest: " +
                sendMessageRequest.getMessageAttributes().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue().getStringValue()).collect(Collectors.joining(", "))
        );
    }
}
