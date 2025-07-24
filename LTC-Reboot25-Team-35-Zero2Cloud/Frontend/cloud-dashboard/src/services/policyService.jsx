
import apiClient from './api';

export const policyService = {
    getAccessPolicies: async () => {
        const response = await apiClient.get('/api/access-policies');
        return response.data;
    }
};

export default policyService;