import React from 'react';
import { Server, Plus, Trash2 } from 'lucide-react';
import { toast } from 'react-toastify';
import { vmService } from '../../services/vmService';
import { useVMForm } from '../../hooks/useVMForm';
import { VM_CONFIG } from '../../constants/vmConstants';
import 'react-toastify/dist/ReactToastify.css';

const VMProvisioning = () => {
  const {
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
  } = useVMForm();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsCreating(true);

    try {
      await vmService.createVM(createForm);
      toast.success('VM created successfully');
      resetCreateForm();
    } catch (error) {
      // Error is handled by axios interceptor
    } finally {
      setIsCreating(false);
    }
  };

  const handleDelete = async (e) => {
    e.preventDefault();

    if (!deleteForm.instanceName || !deleteForm.zone || !deleteForm.projectId) {
      toast.error('All fields are required for VM deletion');
      return;
    }

    setIsDeleting(true);
    try {
      await vmService.deleteVM(deleteForm);
      toast.success('VM deletion initiated successfully');
      resetDeleteForm();
    } catch (error) {
      // Error is handled by axios interceptor
    } finally {
      setIsDeleting(false);
    }
  };

  return (
      <div className="space-y-8">
        {/* Create VM Form */}
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center space-x-3 mb-6">
            <Server className="h-6 w-6 text-blue-600" />
            <h3 className="text-lg font-semibold text-gray-900">Create Virtual Machine</h3>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label htmlFor="projectId" className="block text-sm font-medium text-gray-700 mb-2">
                  Project ID *
                </label>
                <input
                    type="text"
                    id="projectId"
                    name="projectId"
                    value={createForm.projectId}
                    onChange={handleCreateChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="e.g., my-project-id"
                />
              </div>

              <div>
                <label htmlFor="instanceName" className="block text-sm font-medium text-gray-700 mb-2">
                  Instance Name *
                </label>
                <input
                    type="text"
                    id="instanceName"
                    name="instanceName"
                    value={createForm.instanceName}
                    onChange={handleCreateChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="e.g., my-vm-instance"
                />
              </div>

              <div>
                <label htmlFor="zone" className="block text-sm font-medium text-gray-700 mb-2">
                  Zone *
                </label>
                <select
                    id="zone"
                    name="zone"
                    value={createForm.zone}
                    onChange={handleCreateChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  {VM_CONFIG.zones.map((zone) => (
                      <option key={zone} value={zone}>{zone}</option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="machineType" className="block text-sm font-medium text-gray-700 mb-2">
                  Machine Type *
                </label>
                <select
                    id="machineType"
                    name="machineType"
                    value={createForm.machineType}
                    onChange={handleCreateChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  {VM_CONFIG.machineTypes.map((type) => (
                      <option key={type} value={type}>{type}</option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="tags" className="block text-sm font-medium text-gray-700 mb-2">
                  Tags
                </label>
                <input
                    type="text"
                    id="tags"
                    name="tags"
                    value={createForm.tags}
                    onChange={handleCreateChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="e.g., web-server,production"
                />
                <p className="mt-1 text-sm text-gray-500">Separate multiple tags with commas</p>
              </div>

              <div>
                <label htmlFor="disks" className="block text-sm font-medium text-gray-700 mb-2">
                  Disk Type *
                </label>
                <select
                    id="disks"
                    name="disks"
                    value={createForm.disks}
                    onChange={handleCreateChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  {VM_CONFIG.diskTypes.map((type) => (
                      <option key={type} value={type}>{type}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex justify-end space-x-4 pt-6 border-t border-gray-200">
              <button
                  type="button"
                  onClick={resetCreateForm}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 rounded-lg hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-500 transition-colors duration-200"
              >
                Reset
              </button>
              <button
                  type="submit"
                  disabled={isCreating}
                  className={`px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors duration-200 flex items-center space-x-2 ${isCreating ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                <Plus className="h-4 w-4" />
                <span>{isCreating ? 'Creating...' : 'Create VM'}</span>
              </button>
            </div>
          </form>
        </div>

        {/* Delete VM Form */}
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center space-x-3 mb-6">
            <Trash2 className="h-6 w-6 text-red-600" />
            <h3 className="text-lg font-semibold text-gray-900">Delete Virtual Machine</h3>
          </div>

          <form onSubmit={handleDelete} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label htmlFor="deleteProjectId" className="block text-sm font-medium text-gray-700 mb-2">
                  Project ID *
                </label>
                <input
                    type="text"
                    id="deleteProjectId"
                    name="projectId"
                    value={deleteForm.projectId}
                    onChange={handleDeleteChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    placeholder="e.g., my-project-id"
                />
              </div>

              <div>
                <label htmlFor="deleteInstanceName" className="block text-sm font-medium text-gray-700 mb-2">
                  Instance Name *
                </label>
                <input
                    type="text"
                    id="deleteInstanceName"
                    name="instanceName"
                    value={deleteForm.instanceName}
                    onChange={handleDeleteChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    placeholder="e.g., my-vm-instance"
                />
              </div>

              <div>
                <label htmlFor="deleteZone" className="block text-sm font-medium text-gray-700 mb-2">
                  Zone *
                </label>
                <select
                    id="deleteZone"
                    name="zone"
                    value={deleteForm.zone}
                    onChange={handleDeleteChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500"
                >
                  {VM_CONFIG.zones.map((zone) => (
                      <option key={zone} value={zone}>{zone}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex justify-end space-x-4 pt-6 border-t border-gray-200">
              <button
                  type="submit"
                  disabled={isDeleting}
                  className={`px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-lg hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 transition-colors duration-200 flex items-center space-x-2 ${isDeleting ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                <Trash2 className="h-4 w-4" />
                <span>{isDeleting ? 'Deleting...' : 'Delete VM'}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
  );
};

export default VMProvisioning;