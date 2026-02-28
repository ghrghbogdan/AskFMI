import api from './api';

const aiService = {
    /**
     * send question to ai through backend
     * backend handles calling ai api and saving to database
     */
    async sendQuery(query, conversationId = null) {
        const response = await api.post('/chat/query', {
            query,
            conversationId,
        });
        return response.data;
    },

    /**
     * get all messages for a conversation
     */
    async getConversationMessages(conversationId) {
        const response = await api.get(`/chat/conversations/${conversationId}/messages`);
        return response.data;
    },

    /**
     * create new empty conversation
     */
    async createConversation(title = 'New Chat') {
        const response = await api.post('/chat/conversations', { title });
        return response.data;
    },
};

export default aiService;
