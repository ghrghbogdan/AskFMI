// Mock modules before any imports
const mockNavigate = jest.fn();
const mockLogin = jest.fn();

// Mock useNavigate hook
jest.mock('react-router-dom', () => ({
    useNavigate: () => mockNavigate,
}), { virtual: true });

// Mock useAuth hook
jest.mock('../context/AuthContext', () => ({
    useAuth: () => ({
        login: mockLogin,
    }),
}), { virtual: true });

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Login from '../pages/Login';

describe('Login Component', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('renders login form', () => {
        render(<Login />);

        expect(screen.getByText(/welcome back/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /log in/i })).toBeInTheDocument();
    });

    test('successfully submits login', async () => {
        mockLogin.mockResolvedValue({});
        render(<Login />);

        const emailInput = screen.getByLabelText(/email/i);
        const passwordInput = screen.getByLabelText(/password/i);

        fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
        fireEvent.change(passwordInput, { target: { value: 'password123' } });
        fireEvent.submit(screen.getByRole('button').closest('form'));

        await waitFor(() => {
            expect(mockLogin).toHaveBeenCalled();
        });
    });

    test('shows error  on failed login', async () => {
        mockLogin.mockRejectedValue({
            response: { data: { error: 'Invalid credentials' } }
        });

        render(<Login />);

        fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrong' } });
        fireEvent.submit(screen.getByRole('button').closest('form'));

        await waitFor(() => {
            expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
        });
    });
});
