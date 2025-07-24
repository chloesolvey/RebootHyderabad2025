import apiClient from './api';

export const vmService = {
    createVM: async (vmData) => {
        const response = await apiClient.post('/provisioning/vm', vmData);
        return response.data;
    },

    deleteVM: async ({ zone, instanceName, projectId }) => {
        const response = await apiClient.delete(
            `/provisioning/vm/${encodeURIComponent(zone)}/${encodeURIComponent(instanceName)}`,
            { params: { projectId: encodeURIComponent(projectId) } }
        );
        return response.data;
    },

    getAllVMs: async () => {
        const response = await apiClient.get('/api/gcp/all-vms');
        return response.data;
    }

};