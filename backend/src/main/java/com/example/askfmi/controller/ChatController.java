package com.example.askfmi.controller;

import com.example.askfmi.dto.ConversationResponse;
import com.example.askfmi.dto.MessageResponse;
import com.example.askfmi.dto.QueryRequest;
import com.example.askfmi.dto.QueryResponse;
import com.example.askfmi.service.ChatService;
import com.example.askfmi.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/history")
    public ResponseEntity<?> GetChatHistory() {
        Long userId = AuthUtil.getCurrentUserId();

        List<ConversationResponse> conversations = chatService.getUserConversations(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("conversations", conversations != null ? conversations : new ArrayList<>());
        response.put("totalConversations", conversations != null ? conversations.size() : 0);
        return ResponseEntity.ok(response);
    }

    /**
     * send question to ai and get response
     * if conversationId is null, creates new conversation
     */
    @PostMapping("/query")
    public ResponseEntity<?> sendQuery(@RequestBody QueryRequest request) {
        try {
            Long userId = AuthUtil.getCurrentUserId();

            // validate query is not empty
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Query cannot be empty");
                return ResponseEntity.badRequest().body(error);
            }

            // send to ai service and save to database
            QueryResponse response = chatService.sendQuery(
                    userId,
                    request.getQuery(),
                    request.getConversationId()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * get all messages for a conversation
     */
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<?> getConversationMessages(@PathVariable Long id) {
        try {
            Long userId = AuthUtil.getCurrentUserId();

            List<MessageResponse> messages = chatService.getConversationMessages(userId, id);
            return ResponseEntity.ok(messages);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * create new empty conversation
     */
    @PostMapping("/conversations")
    public ResponseEntity<?> createConversation(@RequestBody Map<String, String> request) {
        try {
            Long userId = AuthUtil.getCurrentUserId();
            String title = request.getOrDefault("title", "New Chat");

            ConversationResponse response = chatService.createConversation(userId, title);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
