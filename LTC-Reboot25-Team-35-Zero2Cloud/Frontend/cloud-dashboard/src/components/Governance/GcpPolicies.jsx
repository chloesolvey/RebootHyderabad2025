
import React, { useState, useEffect } from 'react';
import { Shield, Users, Lock, Settings, Loader } from 'lucide-react';
import { policyService } from '../../services/policyService';
import { toast } from 'react-toastify';

const GcpPolicies = () => {
    const [isLoading, setIsLoading] = useState(true);
    const [policies, setPolicies] = useState({
        orgPolicies: [
            {
                name: 'Disable Serial Port Access',
                status: 'Enforced',
                scope: 'Organization-wide',
                lastUpdated: '2025-07-15'
            },
            {
                name: 'Restrict Resource Location',
                status: 'Enforced',
                scope: 'Organization-wide',
                lastUpdated: '2025-07-14'
            },
            {
                name: 'Require OS Login',
                status: 'Enforced',
                scope: 'Project-specific',
                lastUpdated: '2025-07-13'
            }
        ],
        accessPolicies: []
    });

    useEffect(() => {
        fetchPolicies();
    }, []);

    const fetchPolicies = async () => {
        try {
            setIsLoading(true);
            const accessPolicies = await policyService.getAccessPolicies();
            setPolicies(prev => ({
                ...prev,
                accessPolicies
            }));
        } catch (error) {
            console.error('Error fetching policies:', error);
            toast.error('Failed to fetch access policies');
        } finally {
            setIsLoading(false);
        }
    };

    if (isLoading) {
        return (
            <div className="flex items-center justify-center p-8">
                <Loader className="h-8 w-8 text-blue-600 animate-spin" />
                <span className="ml-2 text-gray-600">Loading policies...</span>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Organization Policies */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                <div className="p-6 border-b border-gray-200">
                    <div className="flex items-center space-x-3">
                        <Settings className="h-5 w-5 text-blue-600" />
                        <h3 className="text-lg font-semibold text-gray-900">Organization Policies</h3>
                    </div>
                </div>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Policy Name</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Scope</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Last Updated</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {policies.orgPolicies.map((policy, index) => (
                            <tr key={index} className="hover:bg-gray-50">
                                <td className="px-6 py-4 text-sm text-gray-900">{policy.name}</td>
                                <td className="px-6 py-4">
                                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                                            {policy.status}
                                        </span>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-900">{policy.scope}</td>
                                <td className="px-6 py-4 text-sm text-gray-500">{policy.lastUpdated}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Access Policies */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                <div className="p-6 border-b border-gray-200">
                    <div className="flex items-center space-x-3">
                        <Lock className="h-5 w-5 text-blue-600" />
                        <h3 className="text-lg font-semibold text-gray-900">Access Policies</h3>
                    </div>
                </div>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Role</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Members</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Scope</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Description</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {policies.accessPolicies.map((policy, index) => (
                            <tr key={index} className="hover:bg-gray-50">
                                <td className="px-6 py-4 text-sm font-medium text-gray-900">{policy.role}</td>
                                <td className="px-6 py-4 text-sm text-gray-500">
                                    {policy.members.map((member, i) => (
                                        <div key={i} className="flex items-center space-x-1 mb-1">
                                            <Users className="h-4 w-4 text-gray-400" />
                                            <span>{member}</span>
                                        </div>
                                    ))}
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-900">{policy.scope}</td>
                                <td className="px-6 py-4 text-sm text-gray-500">{policy.description}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default GcpPolicies;