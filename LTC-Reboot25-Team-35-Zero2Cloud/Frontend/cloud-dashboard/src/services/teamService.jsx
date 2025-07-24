
import apiClient from './api';

export const registerTeam = async (projectId, labelValue) => {
    const response = await apiClient.post('/api/gcp/labels/register-team', {
        projectId,
        labelValue,
    });
    return response.data;
};