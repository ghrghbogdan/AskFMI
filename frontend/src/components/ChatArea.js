import React, { useState, useEffect, useRef } from 'react';
import aiService from '../services/aiService';
import chatService from '../services/chatService';
import LoadingAnimation from './LoadingAnimation';

function ChatArea({ conversationId, onConversationCreated, onMessageSent }) {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [loadingHistory, setLoadingHistory] = useState(false);

    // ref for auto-scrolling to bottom
    const messagesEndRef = useRef(null);

    // scroll to bottom of messages
    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    // auto-scroll when messages change
    useEffect(() => {
        scrollToBottom();
    }, [messages, loading]);

    // load conversation messages when conversationId changes
    useEffect(() => {
        if (conversationId) {
            loadConversationMessages(conversationId);
        } else {
            // reset messages when starting new conversation
            setMessages([]);
        }
    }, [conversationId]);

    const loadConversationMessages = async (convId) => {
        setLoadingHistory(true);
        try {
            const messagesData = await chatService.getConversationMessages(convId);
            // transform backend format to ui format
            const formattedMessages = messagesData.map(msg => ({
                role: msg.role,
                content: msg.content
            }));
            setMessages(formattedMessages);
        } catch (err) {
            console.error('Failed to load conversation messages:', err);
            setError('Failed to load conversation history.');
        } finally {
            setLoadingHistory(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!input.trim() || loading) return;

        const userQuestion = input.trim();
        setInput('');
        setError('');

        // add user message to ui immediately
        setMessages((prev) => [
            ...prev,
            { role: 'USER', content: userQuestion },
        ]);

        // show loading animation
        setLoading(true);

        try {
            // call backend which calls ai api and saves to database
            const response = await aiService.sendQuery(userQuestion, conversationId);

            // notify parent component if this is a new conversation
            if (!conversationId && response.conversationId && onConversationCreated) {
                onConversationCreated(response.conversationId);
            }

            // add ai response to ui
            setMessages((prev) => [
                ...prev,
                { role: 'ASSISTANT', content: response.answer },
            ]);

            // notify parent that a message was sent (for sidebar re-sort)
            if (onMessageSent) {
                onMessageSent();
            }

        } catch (err) {
            console.error('Error sending query:', err);
            setError(err.response?.data?.error || 'Failed to get response. Please try again.');

            // remove the user message if request failed
            setMessages((prev) => prev.slice(0, -1));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="chat-area">
            <div className="chat-messages">
                {loadingHistory ? (
                    // show loading while fetching conversation history
                    <div className="loading">Loading conversation...</div>
                ) : messages.length === 0 && !loading ? (
                    // welcome screen shown when no messages exist
                    <div className="welcome-screen">
                        <h1>askFMI</h1>
                        <p>How can I help you today?</p>
                    </div>
                ) : (
                    <>
                        {/* render all messages */}
                        {messages.map((msg, index) => (
                            <div key={index} className={`message ${msg.role.toLowerCase()}`}>
                                <div className="message-content">{msg.content}</div>
                            </div>
                        ))}

                        {/* loading animation while waiting for ai */}
                        {loading && <LoadingAnimation />}

                        {/* invisible element at bottom for auto-scroll */}
                        <div ref={messagesEndRef} />
                    </>
                )}

                {/* error message */}
                {error && (
                    <div className="message error">
                        <div className="message-content">{error}</div>
                    </div>
                )}
            </div>

            <div className="chat-input-container">
                <form onSubmit={handleSubmit} className="chat-form">
                    <input
                        type="text"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        placeholder="Message askFMI..."
                        className="chat-input"
                        disabled={loading || loadingHistory}
                    />
                    <button type="submit" className="send-btn" disabled={loading || loadingHistory || !input.trim()}>
                        {loading ? 'Sending...' : 'Send'}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default ChatArea;
