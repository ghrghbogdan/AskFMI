import axios from 'axios';

// Use relative path so Nginx can proxy it. 
// In dev (npm start), you might need a proxy in package.json.
// In docker/prod, this goes to nginx -> backend.
const API_BASE_URL = '/api';

// create axios instance with base configuration
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// request interceptor - automatically attach jwt token to every api call
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            // add bearer token to authorization header for protected endpoints
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// response interceptor - handle token expiration globally
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // if backend returns 401, token is invalid or expired
        if (error.response?.status === 401) {
            // clear local auth data and force user to login again
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default api;
