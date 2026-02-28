import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Register from '../pages/Register';

// Mock modules
const mockNavigate = jest.fn();
const mockRegister = jest.fn();

jest.mock('react-router-dom', () => ({
    useNavigate: () => mockNavigate,
}), { virtual: true });

jest.mock('../context/AuthContext', () => ({
    useAuth: () => ({
        register: mockRegister,
    }),
}), { virtual: true });

describe('Register Component', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('renders registration form', () => {
        render(<Register />);

        expect(screen.getByText(/create your account/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /sign up/i })).toBeInTheDocument();
    });

    test('shows error when passwords do not match', async () => {
        render(<Register />);

        fireEvent.change(screen.getByLabelText(/^name$/i), { target: { value: 'Test User' } });
        fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByLabelText(/^password$/i), { target: { value: 'password123' } });
        fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'different' } });

        fireEvent.submit(screen.getByRole('button').closest('form'));

        await waitFor(() => {
            expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
        });
    });

    test('successfully submits registration', async () => {
        mockRegister.mockResolvedValue({});
        render(<Register />);

        fireEvent.change(screen.getByLabelText(/^name$/i), { target: { value: 'Test User' } });
        fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByLabelText(/^password$/i), { target: { value: 'password123' } });
        fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'password123' } });

        fireEvent.submit(screen.getByRole('button').closest('form'));

        await waitFor(() => {
            expect(mockRegister).toHaveBeenCalled();
        });
    });

    test('shows error on registration failure', async () => {
        mockRegister.mockRejectedValue({
            response: { data: { error: 'Email already exists' } }
        });

        render(<Register />);

        fireEvent.change(screen.getByLabelText(/^name$/i), { target: { value: 'Test User' } });
        fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByLabelText(/^password$/i), { target: { value: 'password123' } });
        fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'password123' } });

        fireEvent.submit(screen.getByRole('button').closest('form'));

        await waitFor(() => {
            expect(screen.getByText(/email already exists/i)).toBeInTheDocument();
        });
    });
});
