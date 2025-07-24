import { useState } from 'react';
import { VM_CONFIG } from '../constants/vmConstants';

const initialCreateFormState = {
    projectId: '',
    zone: VM_CONFIG.defaultValues.zone,
    instanceName: '',
    machineType: VM_CONFIG.defaultValues.machineType,
    tags: '',
    disks: VM_CONFIG.defaultValues.diskType
};

const initialDeleteFormState = {
    projectId: '',
    zone: VM_CONFIG.defaultValues.zone,
    instanceName: ''
};

export const useVMForm = () => {
    const [createForm, setCreateForm] = useState(initialCreateFormState);
    const [deleteForm, setDeleteForm] = useState(initialDeleteFormState);
    const [isCreating, setIsCreating] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

    const handleCreateChange = (e) => {
        setCreateForm(prev => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
    };

    const handleDeleteChange = (e) => {
        setDeleteForm(prev => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
    };

    const resetCreateForm = () => setCreateForm(initialCreateFormState);
    const resetDeleteForm = () => setDeleteForm(initialDeleteFormState);

    return {
        createForm,
        deleteForm,
        isCreating,
        isDeleting,
        setIsCreating,
        setIsDeleting,
        handleCreateChange,
        handleDeleteChange,
        resetCreateForm,
        resetDeleteForm
    };
};