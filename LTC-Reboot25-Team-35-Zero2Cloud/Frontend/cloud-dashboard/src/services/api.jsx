import axios from 'axios';
import { toast } from 'react-toastify';

const apiClient = axios.create({
    baseURL: 'https://zerotocloud-app-91058824520.us-central1.run.app',
    //baseURL: 'http://localhost:9000',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
    response => response,
    error => {
        const message = error.response?.data || error.message;
        toast.error(`Error: ${message}`);
        return Promise.reject(error);
    }
);

export default apiClient;