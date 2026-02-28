package com.example.askfmi.dto;

public class QueryRequest {
    private String query;
    private Long conversationId; // null for new conversation

    // constructors
    public QueryRequest() {}

    public QueryRequest(String query, Long conversationId) {
        this.query = query;
        this.conversationId = conversationId;
    }

    // getters and setters
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}
