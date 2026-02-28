package com.example.askfmi.service;

import com.example.askfmi.dto.ConversationResponse;
import com.example.askfmi.dto.MessageResponse;
import com.example.askfmi.dto.QueryResponse;
import com.example.askfmi.entity.Conversation;
import com.example.askfmi.entity.Message;
import com.example.askfmi.entity.Role;
import com.example.askfmi.entity.User;
import com.example.askfmi.repository.ConversationRepository;
import com.example.askfmi.repository.MessageRepository;
import com.example.askfmi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiService aiService;

    public List<ConversationResponse> getUserConversations(Long userId) {
        var conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return conversations.stream()
                .map(this::conversationToResponse)
                .toList();
    }

    /**
     * send query to ai and save conversation
     * if conversationId is null, creates new conversation
     * saves both user question and ai response to database
     */
    @Transactional
    public QueryResponse sendQuery(Long userId, String query, Long conversationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation;

        // get existing conversation or create new one
        if (conversationId != null) {
            conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        } else {
            // create new conversation with title from first question
            String title = generateTitle(query);
            conversation = new Conversation(user, title);
            conversation = conversationRepository.save(conversation);
        }

        // create and save user message
        Message userMessage = new Message(conversation, Role.USER, query);
        conversation.addMessage(userMessage);  // this updates conversation.updatedAt
        messageRepository.save(userMessage);

        // call ai api to get response
        String aiAnswer = aiService.query(query);

        // create and save assistant response
        Message assistantMessage = new Message(conversation, Role.ASSISTANT, aiAnswer);
        conversation.addMessage(assistantMessage);  // this updates conversation.updatedAt
        messageRepository.save(assistantMessage);

        // save conversation with updated timestamp
        conversation = conversationRepository.save(conversation);

        return new QueryResponse(
                aiAnswer,
                conversation.getId(),
                assistantMessage.getId(),
                conversation.getTitle()
        );
    }

    /**
     * get all messages for a conversation
     * returns messages in chronological order
     */
    public List<MessageResponse> getConversationMessages(Long userId, Long conversationId) {
        // verify user owns this conversation
        conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        var messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream()
                .map(this::messageToResponse)
                .toList();
    }

    /**
     * create new empty conversation
     */
    public ConversationResponse createConversation(Long userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = new Conversation(user, title);
        conversation = conversationRepository.save(conversation);

        return conversationToResponse(conversation);
    }

    /**
     * generate conversation title from first question
     * truncates to 50 characters for display
     */
    private String generateTitle(String query) {
        if (query.length() <= 50) {
            return query;
        }
        return query.substring(0, 47) + "...";
    }

    private ConversationResponse conversationToResponse(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private MessageResponse messageToResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
