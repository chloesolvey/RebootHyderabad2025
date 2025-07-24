import React, { useState } from 'react';
import { BarChart3, Clock } from 'lucide-react';
import Charts from './Charts.jsx';
import TimeFilter from './TimeFilter.jsx';
import { TimeFilters } from '../../types/index.js';

const SERVICE_TYPES = ['Bucket-Bytes',
        'Bucket-Objects',
        'VM-CPU',
        'VM-Memory',
        'CloudSQL-Connections',
        'CloudSQL-CPU'];

const MonitoringTab = () => {
  const [selectedTimeFilter, setSelectedTimeFilter] = useState(TimeFilters.ONE_HOUR);
  const [customRange, setCustomRange] = useState({
    from: '',
    to: ''
  });
    const [serviceType, setServiceType] = useState(SERVICE_TYPES[0]);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <div className="flex items-center space-x-3 mb-2">
              <BarChart3 className="h-6 w-6 text-blue-600" />
              <h2 className="text-2xl font-bold text-gray-900">Infrastructure Monitoring</h2>
            </div>
            <p className="text-gray-600">Real-time performance metrics and analytics</p>
          </div>
          
          <div className="flex items-center space-x-3">
            <Clock className="h-5 w-5 text-gray-400" />
          </div>
        </div>
      </div>

      <div className="mb-6">
        <TimeFilter
          selectedFilter={selectedTimeFilter}
          onFilterChange={setSelectedTimeFilter}
          customRange={customRange}
          onCustomRangeChange={setCustomRange}
          serviceType={serviceType}
          onServiceTypeChange={setServiceType}
        />
      </div>

      <Charts timeFilter={selectedTimeFilter} customRange={customRange} serviceType={serviceType}/>
    </div>
  );
};

export default MonitoringTab;