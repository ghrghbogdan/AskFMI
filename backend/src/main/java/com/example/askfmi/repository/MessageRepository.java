package com.example.askfmi.repository;

import com.example.askfmi.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // find all messages for a conversation, ordered chronologically
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    
    // count messages in a conversation
    long countByConversationId(Long conversationId);
}
