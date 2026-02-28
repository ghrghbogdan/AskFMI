package com.example.askfmi.service;

import com.example.askfmi.dto.ConversationResponse;
import com.example.askfmi.dto.QueryResponse;
import com.example.askfmi.entity.Conversation;
import com.example.askfmi.entity.Message;
import com.example.askfmi.entity.Role;
import com.example.askfmi.entity.User;
import com.example.askfmi.repository.ConversationRepository;
import com.example.askfmi.repository.MessageRepository;
import com.example.askfmi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ChatService chatService;

    private User testUser;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "test@example.com", "hashedpassword");
        testUser.setId(1L);

        testConversation = new Conversation(testUser, "Test Conversation");
        testConversation.setId(1L);
    }

    @Test
    void getUserConversations_shouldReturnConversationList() {
        when(conversationRepository.findByUserIdOrderByUpdatedAtDesc(1L))
                .thenReturn(List.of(testConversation));

        List<ConversationResponse> result = chatService.getUserConversations(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Conversation", result.get(0).getTitle());
    }

    @Test
    void sendQuery_withNewConversation_shouldCreateConversation() {
        String query = "What is the admission process?";
        String aiResponse = "The admission process involves...";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
        when(aiService.query(query)).thenReturn(aiResponse);
        when(messageRepository.save(any(Message.class))).thenReturn(new Message());

        QueryResponse response = chatService.sendQuery(1L, query, null);

        assertNotNull(response);
        assertEquals(aiResponse, response.getAnswer());
        assertNotNull(response.getConversationId());
        verify(conversationRepository, times(2)).save(any(Conversation.class)); // once for creation, once after messages
        verify(messageRepository, times(2)).save(any(Message.class)); // user message + ai response
    }

    @Test
    void sendQuery_withExistingConversation_shouldAppendMessages() {
        String query = "Follow-up question";
        String aiResponse = "Follow-up answer";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(conversationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testConversation));
        when(aiService.query(query)).thenReturn(aiResponse);
        when(messageRepository.save(any(Message.class))).thenReturn(new Message());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        QueryResponse response = chatService.sendQuery(1L, query, 1L);

        assertNotNull(response);
        assertEquals(aiResponse, response.getAnswer());
        assertEquals(1L, response.getConversationId());
        verify(messageRepository, times(2)).save(any(Message.class));
    }

    @Test
    void sendQuery_userNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            chatService.sendQuery(1L, "query", null);
        });
    }

    @Test
    void createConversation_shouldReturnNewConversation() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        ConversationResponse response = chatService.createConversation(1L, "New Chat");

        assertNotNull(response);
        assertEquals("Test Conversation", response.getTitle());
        verify(conversationRepository).save(any(Conversation.class));
    }
}
