import { render, screen } from '@testing-library/react';
import ChatArea from '../components/ChatArea';

// Mock the services
jest.mock('../services/aiService', () => ({
    sendQuery: jest.fn(),
}));

jest.mock('../services/chatService', () => ({
    getConversationMessages: jest.fn(),
}));

describe('ChatArea Component', () => {
    test('renders welcome screen when no messages', () => {
        render(<ChatArea conversationId={null} />);

        expect(screen.getByText(/askFMI/i)).toBeInTheDocument();
        expect(screen.getByText(/how can i help you today/i)).toBeInTheDocument();
    });

    test('renders input field and send button', () => {
        render(<ChatArea conversationId={null} />);

        const input = screen.getByPlaceholderText(/message askfmi/i);
        const button = screen.getByRole('button', { name: /send/i });

        expect(input).toBeInTheDocument();
        expect(button).toBeInTheDocument();
    });

    test('send button is disabled when input is empty', () => {
        render(<ChatArea conversationId={null} />);

        const button = screen.getByRole('button', { name: /send/i });

        expect(button).toBeDisabled();
    });
});
