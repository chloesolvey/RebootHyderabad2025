export const mockVMs = [
  {
    id: 'vm-001',
    name: 'web-server-01',
    os: 'Ubuntu 22.04',
    size: 't3.medium',
    region: 'us-east-1',
    status: 'running',
    createdAt: '2025-01-01T10:00:00Z',
    cpuUsage: 45,
    memoryUsage: 62
  },
  {
    id: 'vm-002',
    name: 'database-01',
    os: 'CentOS 8',
    size: 't3.large',
    region: 'us-west-2',
    status: 'running',
    createdAt: '2024-12-28T14:30:00Z',
    cpuUsage: 78,
    memoryUsage: 84
  },
  {
    id: 'vm-003',
    name: 'app-server-01',
    os: 'Windows Server 2022',
    size: 't3.small',
    region: 'eu-west-1',
    status: 'stopped',
    createdAt: '2024-12-30T09:15:00Z',
    cpuUsage: 0,
    memoryUsage: 0
  }
];

export const mockClusters = [
  {
    id: 'cluster-001',
    name: 'prod-k8s-cluster',
    nodeCount: 3,
    nodeSize: 't3.medium',
    region: 'us-east-1',
    status: 'healthy',
    createdAt: '2024-12-25T12:00:00Z',
    cpuUsage: 56,
    memoryUsage: 71
  },
  {
    id: 'cluster-002',
    name: 'dev-k8s-cluster',
    nodeCount: 2,
    nodeSize: 't3.small',
    region: 'us-west-2',
    status: 'scaling',
    createdAt: '2024-12-29T16:45:00Z',
    cpuUsage: 34,
    memoryUsage: 48
  }
];

export const generateChartData = async(timeframe,startTime,endTime,serviceType) => {
  let data = [];

  console.log(serviceType);

  if(serviceType === 'Bucket-Bytes') {
  const bucketBytesData = await fetchBucketBytes(timeframe,startTime,endTime);
  let bbytesData = [];
  if(bucketBytesData && bucketBytesData.points) {
  bucketBytesData.points.forEach((bucketData, index) => {
    bbytesData.push({
      timestamp: new Date(bucketData.timestamp).toISOString(),
      component: bucketData.value
  });
  });
  }
  data = bbytesData;
  }
  else if(serviceType === 'Bucket-Objects') {
  const bucketObjectsData = await fetchBucketObjects(timeframe,startTime,endTime);
  let bObjectsData = [];
  if(bucketObjectsData && bucketObjectsData.points) {
  bucketObjectsData.points.forEach((bucketData, index) => {
    bObjectsData.push({
      timestamp: new Date(bucketData.timestamp).toISOString(),
      component: bucketData.value
  });
  });
}
  data = bObjectsData;
  }
  else if(serviceType === 'VM-CPU') {
  const vmCpuData = await fetchVmCpu(timeframe,startTime,endTime);
  let cpuData = [];
  if(vmCpuData && vmCpuData.points) {
  vmCpuData.points.forEach((cpu, index) => {
    cpuData.push({
      timestamp: new Date(cpu.timestamp).toISOString(),
      component: cpu.value
  });
  });
}
  data = cpuData;
  }
  else if(serviceType === 'VM-Memory') {
  const vmMemoryData = await fetchVmMemory(timeframe,startTime,endTime);
  let memData = [];
  if(vmMemoryData && vmMemoryData.points) {
  vmMemoryData.points.forEach((mem, index) => {
    memData.push({
      timestamp: new Date(mem.timestamp).toISOString(),
      bucket: mem.value
  });
  });
}
  data = memData;
  }
  else if(serviceType === 'CloudSQL-Connections') {
  const cloudConData = await fetchCloudConn(timeframe,startTime,endTime);
  let cloudData = [];
  if(cloudConData && cloudConData.points) {
  cloudConData.points.forEach((con, index) => {
    cloudData.push({
      timestamp: new Date(con.timestamp).toISOString(),
      bucket: con.value
  });
  });
}
  data = cloudData;
  }
  else if(serviceType === 'CloudSQL-CPU') {
  const cloudCpuData = await fetchCloudCpu(timeframe,startTime,endTime);
  let cpuData = [];
  if(cloudCpuData && cloudCpuData.points) {
  cloudCpuData.points.forEach((con, index) => {
    cpuData.push({
      timestamp: new Date(con.timestamp).toISOString(),
      component: con.value
  });
  });
}
  data = cpuData;
  }
    
  return data;
};

const MONITORING_BASE_URL = 'http://localhost:9000/api/v1';

export const fetchBucketBytes= async (timeframe,startTime,endTime) => {
  let data = [];
    try {
      const response = await fetch(`${MONITORING_BASE_URL}/bucket/bytes`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
                timeframe: timeframe,
                startTime: startTime,
                endTime: endTime // Default time range, can be adjusted
              })   
      });

      if (response.ok) {
      data = response.json();
      }
      return data;
    } catch (error) {
     console.log(error);
    }
  };

  export const fetchVmCpu= async (timeframe,startTime,endTime) => {
    try {
      const response = await fetch(`${MONITORING_BASE_URL}/vm/cpu`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
                timeframe: timeframe,
                startTime: startTime,
                endTime: endTime 
              })   
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = response.json();
      return data;
    } catch (error) {
      console.error('Error fetching bucket bytes:', error);
      throw error;
    }
  };

  export const fetchBucketObjects= async (timeframe,startTime,endTime) => {
    try {
      const response = await fetch(`${MONITORING_BASE_URL}/bucket/objects`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
                timeframe: timeframe,
                startTime: startTime,
                endTime: endTime // Default time range, can be adjusted
              })   
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = response.json();
      return data;
    } catch (error) {
      console.error('Error fetching bucket bytes:', error);
      throw error;
    }
  };

  export const fetchVmMemory= async (timeframe,startTime,endTime) => {
    try {
      const response = await fetch(`${MONITORING_BASE_URL}/vm/memory`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
                timeframe: timeframe,
                startTime: startTime,
                endTime: endTime // Default time range, can be adjusted
              })   
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = response.json();
      return data;
    } catch (error) {
      console.error('Error fetching bucket bytes:', error);
      throw error;
    }
  };

  export const fetchCloudConn= async (timeframe,startTime,endTime) => {
    try {
      const response = await fetch(`${MONITORING_BASE_URL}/cloudsql/connections`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
                timeframe: timeframe,
                startTime: startTime,
                endTime: endTime // Default time range, can be adjusted
              })   
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = response.json();
      return data;
    } catch (error) {
      console.error('Error fetching bucket bytes:', error);
      throw error;
    }
  };

  export const fetchCloudCpu= async (timeframe,startTime,endTime) => {
    try {
      const response = await fetch(`${MONITORING_BASE_URL}/cloudsql/cpu`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
                timeframe: timeframe,
                startTime: startTime,
                endTime: endTime // Default time range, can be adjusted
              })   
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = response.json();
      return data;
    } catch (error) {
      console.error('Error fetching bucket bytes:', error);
      throw error;
    }
  };
