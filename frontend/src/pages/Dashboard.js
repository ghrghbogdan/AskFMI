import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import ChatArea from '../components/ChatArea';
import chatService from '../services/chatService';

function Dashboard() {
    const [conversations, setConversations] = useState([]);
    const [currentConversationId, setCurrentConversationId] = useState(null);

    // load conversation history on mount
    useEffect(() => {
        loadConversations();
    }, []);

    const loadConversations = async () => {
        try {
            const data = await chatService.getChatHistory();
            setConversations(data.conversations || []);
        } catch (err) {
            console.error('Failed to load conversations:', err);
        }
    };

    // callback when new conversation is created from chat
    const handleConversationCreated = (conversationId) => {
        setCurrentConversationId(conversationId);
        // reload sidebar to show new conversation
        loadConversations();
    };

    // callback after any message is sent (new or existing conversation)
    // this ensures sidebar re-sorts to show most recent conversation on top
    const handleMessageSent = () => {
        loadConversations();
    };

    return (
        <div className="dashboard">
            <Sidebar
                conversations={conversations}
                currentConversationId={currentConversationId}
                onConversationSelect={setCurrentConversationId}
            />
            <ChatArea
                conversationId={currentConversationId}
                onConversationCreated={handleConversationCreated}
                onMessageSent={handleMessageSent}
            />
        </div>
    );
}

export default Dashboard;
