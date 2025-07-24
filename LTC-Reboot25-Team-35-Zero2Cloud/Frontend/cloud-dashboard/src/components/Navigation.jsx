import React from 'react';
import { Server, Shield, BarChart3 } from 'lucide-react';
import { TabTypes } from '../types/index.js';

const Navigation = ({ activeTab, onTabChange }) => {
  const tabs = [
    { id: TabTypes.PROVISIONING, label: 'Provisioning', icon: Server },
    { id: TabTypes.GOVERNANCE, label: 'Governance', icon: Shield },
    { id: TabTypes.MONITORING, label: 'Monitoring', icon: BarChart3 }
  ];

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex space-x-8">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            
            return (
              <button
                key={tab.id}
                onClick={() => onTabChange(tab.id)}
                className={`flex items-center space-x-2 py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200 ${
                  isActive
                    ? 'border-blue-600 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <Icon className="h-5 w-5" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>
      </div>
    </nav>
  );
};

export default Navigation;