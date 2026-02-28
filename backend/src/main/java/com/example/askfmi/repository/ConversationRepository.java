package com.example.askfmi.repository;

import com.example.askfmi.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // find all conversations for a given user, order by most recent
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    // find specific conversation by id and user id
    Optional<Conversation> findByIdAndUserId(Long id, Long userId);

    // counter
    long countByUserId(Long userId);
}
