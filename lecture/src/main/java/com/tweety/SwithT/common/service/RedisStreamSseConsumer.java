package com.tweety.SwithT.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisStreamSseConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final ConcurrentHashMap<String, SseEmitter> clients = new ConcurrentHashMap<>();

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        String recordId = record.getId().getValue(); // Redis Stream 고유 ID
        String memberId = record.getValue().get("memberId");
        String messageType = record.getValue().get("messageType");
        String title = record.getValue().get("title");
        String contents = record.getValue().get("contents");

        SseEmitter emitter = clients.get(memberId);

        if(emitter != null){
            try {
                Map<String, String> structuredMessage = new HashMap<>();
                structuredMessage.put("id", recordId); // Redis Stream 고유 ID
                structuredMessage.put("messageType", messageType);
                structuredMessage.put("title", title);
                structuredMessage.put("contents", contents);

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonMessage = objectMapper.writeValueAsString(structuredMessage);

                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(jsonMessage));


            } catch (IOException e) {
                emitter.completeWithError(e);
                clients.remove(memberId);
            }
        } else {
            System.out.println("No emitter found for memberId: " + memberId); // 클라이언트가 없을 때 로그
        }
    }

    public void addClient(String memberId, SseEmitter emitter) {
        clients.put(memberId, emitter);
    }

    public void removeClient(String memberId) {
        clients.remove(memberId);
    }
}
