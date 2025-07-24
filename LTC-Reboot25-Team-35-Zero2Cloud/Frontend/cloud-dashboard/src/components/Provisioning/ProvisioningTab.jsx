
import React from 'react';
import { Server } from 'lucide-react';
import VMProvisioning from './VMProvisioning.jsx';

const ProvisioningTab = () => {
    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="mb-8">
                <h2 className="text-2xl font-bold text-gray-900 mb-2">Infrastructure Provisioning</h2>
                <p className="text-gray-600">Create and manage your virtual machines</p>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                <div className="animate-fadeIn">
                    <VMProvisioning />
                </div>
            </div>
        </div>
    );
};

export default ProvisioningTab;