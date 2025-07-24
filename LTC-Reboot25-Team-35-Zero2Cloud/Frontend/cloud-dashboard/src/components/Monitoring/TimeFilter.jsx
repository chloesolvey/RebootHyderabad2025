import React from 'react';
import { Calendar } from 'lucide-react';
import { TimeFilters } from '../../types/index.js';

const SERVICE_TYPES = ['Bucket-Bytes',
        'Bucket-Objects',
        'VM-CPU',
        'VM-Memory',
        'CloudSQL-Connections',
        'CloudSQL-CPU'];

const TimeFilter = ({ selectedFilter, onFilterChange, customRange, onCustomRangeChange,serviceType,
  onServiceTypeChange }) => {
  const filters = [
    { id: TimeFilters.ONE_HOUR, label: 'Last Hour' },
    { id: TimeFilters.ONE_DAY, label: 'Last Day' },
    { id: TimeFilters.ONE_WEEK, label: 'Last Week' },
    { id: TimeFilters.CUSTOM, label: 'Custom Range' }
  ];

  const handleDateChange = (field, value) => {
    onCustomRangeChange({
      ...customRange,
      [field]: value
    });
  };

  const formatDateForInput = (date) => {
    if (!date) return '';
    return new Date(date).toISOString().slice(0, 16);
  };

  return (

    <div className="space-y-4">
      <div className="flex items-center space-x-4">
    {/* ...existing time filter controls... */}
    <select
      value={serviceType}
      onChange={e => onServiceTypeChange(e.target.value)}
      className="border rounded px-2 py-1"
    >
      {SERVICE_TYPES.map(type => (
        <option key={type} value={type}>{type}</option>
      ))}
    </select>
  </div>
      <div className="flex space-x-1 bg-gray-100 rounded-lg p-1">
        {filters.map((filter) => (
          <button
            key={filter.id}
            onClick={() => onFilterChange(filter.id)}
            className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors duration-200 ${
              selectedFilter === filter.id
                ? 'bg-white text-blue-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            {filter.label}
          </button>
        ))}
      </div>
      
      {selectedFilter === TimeFilters.CUSTOM && (
        <div className="bg-white rounded-lg border border-gray-200 p-4 shadow-sm">
          <div className="flex items-center space-x-2 mb-3">
            <Calendar className="h-4 w-4 text-blue-600" />
            <h4 className="text-sm font-medium text-gray-900">Select Date Range</h4>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="fromDate" className="block text-xs font-medium text-gray-700 mb-1">
                From Date & Time
              </label>
              <input
                type="datetime-local"
                id="fromDate"
                value={formatDateForInput(customRange.from)}
                onChange={(e) => handleDateChange('from', e.target.value)}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
              {customRange.from && (
                <p className="text-xs text-gray-500 mt-1">
                  {new Date(customRange.from).toLocaleString()}
                </p>
              )}
            </div>
            <div>
              <label htmlFor="toDate" className="block text-xs font-medium text-gray-700 mb-1">
                To Date & Time
              </label>
              <input
                type="datetime-local"
                id="toDate"
                value={formatDateForInput(customRange.to)}
                onChange={(e) => handleDateChange('to', e.target.value)}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
              {customRange.to && (
                <p className="text-xs text-gray-500 mt-1">
                  {new Date(customRange.to).toLocaleString()}
                </p>
              )}
            </div>
          </div>
          {customRange.from && customRange.to && (
            <div className="mt-3 p-2 bg-blue-50 rounded-md">
              <p className="text-xs text-blue-700">
                <strong>Selected Range:</strong> {new Date(customRange.from).toLocaleString()} to {new Date(customRange.to).toLocaleString()}
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default TimeFilter;