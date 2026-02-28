import api from './api';

const authService = {
    async login(email, password) {
        const response = await api.post('/auth/login', { email, password });

        // save jwt token and user data to browser storage for persistence
        if (response.data.token) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data.user));
        }
        return response.data;
    },

    async register(name, email, password, confirmPassword) {
        const response = await api.post('/auth/register', {
            name,
            email,
            password,
            confirmPassword,
        });

        // auto-login after successful registration by saving token
        if (response.data.token) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data.user));
        }
        return response.data;
    },

    logout() {
        // clear all auth data from browser storage
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    getCurrentUser() {
        // parse user object from json string in storage
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    },

    getToken() {
        return localStorage.getItem('token');
    },

    isAuthenticated() {
        // check if valid token exists in storage
        return !!this.getToken();
    },
};

export default authService;
