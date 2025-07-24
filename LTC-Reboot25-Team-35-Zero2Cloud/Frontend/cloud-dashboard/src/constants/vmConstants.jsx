export const VM_CONFIG = {
    zones: [
        'us-central1-a',
        'us-central1-b',
        'us-central1-c',
        'us-east1-b',
        'us-west1-a',
        'europe-west1-b',
        'asia-east1-a'
    ],
    machineTypes: [
        'e2-micro',
        'e2-small',
        'e2-medium',
        'e2-standard-2',
        'e2-standard-4',
        'e2-standard-8'
    ],
    diskTypes: [
        'pd-standard',
        'pd-balanced',
        'pd-ssd'
    ],
    defaultValues: {
        zone: 'us-central1-a',
        machineType: 'e2-medium',
        diskType: 'pd-standard'
    }
};