import axios from 'axios';

// Prefer explicit API URL; fallback to CRA proxy path
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
axiosInstance.interceptors.response.use(
  (response) => {
    // For blob downloads (e.g., Excel export), return full response so headers remain available.
    if (response.config && response.config.responseType === 'blob') {
      return response;
    }
    return response.data;
  },
  (error) => {
    if (error.response) {
      // Preserve the full response for blob errors; otherwise return parsed data.
      if (error.response.config && error.response.config.responseType === 'blob') {
        return Promise.reject(error.response);
      }
      return Promise.reject(error.response.data || error.response);
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
