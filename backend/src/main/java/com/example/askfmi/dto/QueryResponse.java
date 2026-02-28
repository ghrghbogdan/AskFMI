package com.example.askfmi.dto;

public class QueryResponse {
    private String answer;
    private Long conversationId;
    private Long messageId;
    private String conversationTitle;

    // constructors
    public QueryResponse() {}

    public QueryResponse(String answer, Long conversationId, Long messageId, String conversationTitle) {
        this.answer = answer;
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.conversationTitle = conversationTitle;
    }

    // getters and setters
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getConversationTitle() {
        return conversationTitle;
    }

    public void setConversationTitle(String conversationTitle) {
        this.conversationTitle = conversationTitle;
    }
}
