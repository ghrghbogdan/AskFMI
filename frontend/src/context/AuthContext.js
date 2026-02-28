import React, { createContext, useState, useContext, useEffect } from 'react';
import authService from '../services/authService';

// global context for authentication state - accessible from any component
const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // on app mount, check if user was previously logged in
        // this enables auto-login when user refreshes page
        const currentUser = authService.getCurrentUser();
        if (currentUser) {
            setUser(currentUser);
        }
        setLoading(false);
    }, []);

    const login = async (email, password) => {
        // call backend api and update global state on success
        const data = await authService.login(email, password);
        setUser(data.user);
        return data;
    };

    const register = async (name, email, password, confirmPassword) => {
        // create account and immediately log user in
        const data = await authService.register(name, email, password, confirmPassword);
        setUser(data.user);
        return data;
    };

    const logout = () => {
        // clear storage and reset global state
        authService.logout();
        setUser(null);
    };

    // expose these functions and state to all child components
    const value = {
        user,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        loading,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// custom hook for accessing auth context in components
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
