import React from 'react';
import { Server, Layers, Calendar, MapPin, Activity } from 'lucide-react';

const ResourceList = ({ title, type, resources }) => {
  const getStatusColor = (status) => {
    switch (status) {
      case 'running':
      case 'healthy':
        return 'bg-green-100 text-green-800';
      case 'stopped':
        return 'bg-gray-100 text-gray-800';
      case 'pending':
      case 'scaling':
        return 'bg-yellow-100 text-yellow-800';
      case 'error':
      case 'unhealthy':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const Icon = type === 'vm' ? Server : Layers;

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <Icon className="h-5 w-5 text-blue-600" />
            <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
            <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-0.5 rounded-full">
              {resources.length} {resources.length === 1 ? 'resource' : 'resources'}
            </span>
          </div>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Name
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                {type === 'vm' ? 'OS/Size' : 'Nodes/Size'}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Region
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Usage
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Created
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {resources.map((resource) => (
              <tr key={resource.id} className="hover:bg-gray-50 transition-colors duration-150">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 h-8 w-8">
                      <Icon className="h-8 w-8 text-gray-400" />
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900">{resource.name}</div>
                      <div className="text-sm text-gray-500">{resource.id}</div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(resource.status)}`}>
                    {resource.status}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {type === 'vm' ? (
                    <div>
                      <div>{resource.os}</div>
                      <div className="text-gray-500">{resource.size}</div>
                    </div>
                  ) : (
                    <div>
                      <div>{resource.nodeCount} nodes</div>
                      <div className="text-gray-500">{resource.nodeSize}</div>
                    </div>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  <div className="flex items-center">
                    <MapPin className="h-4 w-4 text-gray-400 mr-1" />
                    {resource.region}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  <div className="flex items-center space-x-4">
                    <div className="flex items-center">
                      <Activity className="h-4 w-4 text-blue-500 mr-1" />
                      <span>CPU: {resource.cpuUsage}%</span>
                    </div>
                    <div className="flex items-center">
                      <Activity className="h-4 w-4 text-green-500 mr-1" />
                      <span>MEM: {resource.memoryUsage}%</span>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  <div className="flex items-center">
                    <Calendar className="h-4 w-4 text-gray-400 mr-1" />
                    {formatDate(resource.createdAt)}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {resources.length === 0 && (
        <div className="text-center py-12">
          <Icon className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">No resources found</h3>
          <p className="mt-1 text-sm text-gray-500">
            No {title.toLowerCase()} match your current filters.
          </p>
        </div>
      )}
    </div>
  );
};

export default ResourceList;