import React, { useState,useEffect, useMemo } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import { TimeFilters } from '../../types/index.js';
import { generateChartData } from '../../data/mockData.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const Charts = ({ timeFilter, customRange, serviceType }) => {
  const [chartData, setChartData] = useState([]);

  useEffect(() => {
    let hours = 24;
    let timeframe = 'LAST_HOUR';
    let fromDate = '';
    let toDate = '';
    
    if (timeFilter === TimeFilters.CUSTOM && customRange.from && customRange.to) {
      fromDate = new Date(customRange.from);
      toDate = new Date(customRange.to);
      const diffInMs = toDate.getTime() - fromDate.getTime();
      hours = Math.ceil(diffInMs / (1000 * 60 * 60)); // Convert to hours
      hours = Math.max(1, hours); // Ensure at least 1 hour
    } else {
      switch (timeFilter) {
        case TimeFilters.ONE_HOUR:
          hours = 1;
          timeframe = 'LAST_HOUR';
          fromDate = '';
          toDate = '';
          break;
        case TimeFilters.ONE_DAY:
          hours = 24;
          timeframe = 'LAST_DAY';
          fromDate = '';
          toDate = '';
          break;
        case TimeFilters.ONE_WEEK:
          hours = 168; // 7 days * 24 hours
          timeframe = 'LAST_WEEK';
          fromDate = '';
          toDate = '';
          break;
        default:
          hours = 24;
          timeframe = 'LAST_DAY'; // Default to last 24 hours
          fromDate = '';
          toDate = '';
      }
    }
    
   // Fetch chart data asynchronously
    const fetchData = async () => {
      const data = await generateChartData(timeframe, fromDate, toDate, serviceType);
      setChartData(Array.isArray(data) ? data : []);
    };

    fetchData();
  }, [timeFilter, customRange, serviceType]);

  const labels = chartData.map(data => {
    const date = new Date(data.timestamp);
    if (timeFilter === TimeFilters.ONE_HOUR || (timeFilter === TimeFilters.CUSTOM && customRange.from && customRange.to && 
        new Date(customRange.to).getTime() - new Date(customRange.from).getTime() <= 3600000)) {
      return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    } else if (timeFilter === TimeFilters.ONE_DAY || (timeFilter === TimeFilters.CUSTOM && customRange.from && customRange.to && 
             new Date(customRange.to).getTime() - new Date(customRange.from).getTime() <= 86400000)) {
      return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    } else {
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }
  });
console.log('Chart Data:', chartData);
  const displayData = useMemo(() => ({
    labels,
    datasets: [
      {
        label: serviceType+' Utilization',
        data: chartData.map(data => data.component),
        borderColor: 'rgb(59, 130, 246)',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        fill: true,
        tension: 0.4,
      }
    ]
  }), [labels, chartData, serviceType]);

  console.log('Display Data:', displayData);
  let yminValue = 0;
  let ymaxValue = 100;
  if(serviceType == 'Bucket-Bytes' || serviceType == 'Bucket-Objects') {
    yminValue = 0;
    ymaxValue = 2000000; 
  }else if(serviceType == 'VM-CPU' || serviceType == 'VM-Memory') {
    yminValue = 0;
    ymaxValue = 1; 
  }else if(serviceType == 'CloudSQL-Connections' || serviceType == 'CloudSQL-CPU') {
    yminValue = 0;
    ymaxValue = 1000;
  }
  const options = { 
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
      },
      tooltip: {
        mode: 'index',
        intersect: false,
      },
    },
    scales: {
      x: {
        display: true,
        title: {
          display: true,
          text: 'Time'
        }
      },
      y: {
        display: true,
        title: {
          display: true,
          text: 'Values'
        },
        min: yminValue,
        max: ymaxValue
      }
    },
    interaction: {
      mode: 'nearest',
      axis: 'x',
      intersect: false,
    },
  };

  //const averageCpu = chartData.reduce((sum, data) => sum + data.cpu, 0) / chartData.length;
  //const averageMemory = chartData.reduce((sum, data) => sum + data.memory, 0) / chartData.length;

  return (
    <div className="space-y-6">
      {/* Summary Cards */}

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">{serviceType} Utilization</h3>
          <div className="h-80">
            <Line data={displayData} options={options} />
          </div>
        </div>

      </div>


    </div>
  );
};

export default Charts;