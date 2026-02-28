import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

function Sidebar({ conversations, currentConversationId, onConversationSelect }) {
    const { user, logout } = useAuth();
    const [showProfile, setShowProfile] = useState(false);

    const handleLogout = () => {
        logout();
    };

    const handleNewChat = () => {
        // reset to new conversation
        onConversationSelect(null);
    };

    const handleConversationClick = (conversationId) => {
        onConversationSelect(conversationId);
    };

    return (
        <div className="sidebar">
            <div className="sidebar-header">
                <h2>askFMI</h2>
                <button className="new-chat-btn" onClick={handleNewChat}>
                    + New chat
                </button>
            </div>

            <div className="sidebar-content">
                <div className="chat-history">
                    {conversations.length === 0 ? (
                        <p className="empty-state">No conversations yet</p>
                    ) : (
                        conversations.map((conv) => (
                            <div
                                key={conv.id}
                                className={`conversation-item ${conv.id === currentConversationId ? 'active' : ''}`}
                                onClick={() => handleConversationClick(conv.id)}
                            >
                                <div className="conversation-title">{conv.title}</div>
                            </div>
                        ))
                    )}
                </div>
            </div>

            <div className="sidebar-footer">
                <div className="user-menu">
                    <button
                        className="user-button"
                        onClick={() => setShowProfile(!showProfile)}
                    >
                        {/* avatar shows first letter of user's name */}
                        <div className="user-avatar">
                            {user?.name?.charAt(0).toUpperCase()}
                        </div>
                        <span className="user-name">{user?.name}</span>
                    </button>

                    {/* dropdown menu toggles on user button click */}
                    {showProfile && (
                        <div className="user-dropdown">
                            <div className="user-info">
                                <p className="user-email">{user?.email}</p>
                            </div>
                            <button onClick={handleLogout} className="logout-btn">
                                Log out
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default Sidebar;
