import api from './api';

const chatService = {
    // get user's chat history (list of conversations)
    async getChatHistory() {
        const response = await api.get('/chat/history');
        return response.data;
    },

    // get messages for a specific conversation
    async getConversationMessages(conversationId) {
        const response = await api.get(`/chat/conversations/${conversationId}/messages`);
        return response.data;
    },
};

export default chatService;
