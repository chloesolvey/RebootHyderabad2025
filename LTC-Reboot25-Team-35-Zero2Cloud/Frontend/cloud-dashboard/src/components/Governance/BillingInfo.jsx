import React from 'react';
import { DollarSign, TrendingUp, Calendar, AlertCircle } from 'lucide-react';

const BillingInfo = ({ billingData }) => {
    // This would come from your actual GCP billing API
    const mockBillingData = {
        currentMonthSpend: 2458.32,
        previousMonthSpend: 2100.50,
        projectedSpend: 2890.45,
        budgetLimit: 3000.00,
        topServices: [
            { name: 'Compute Engine', cost: 1200.50 },
            { name: 'Cloud Storage', cost: 450.30 },
            { name: 'Cloud SQL', cost: 380.25 },
            { name: 'Kubernetes Engine', cost: 290.15 },
        ],
        recentCharges: [
            { date: '2025-07-16', amount: 85.20, service: 'Compute Engine' },
            { date: '2025-07-15', amount: 42.15, service: 'Cloud Storage' },
            { date: '2025-07-14', amount: 63.30, service: 'Cloud SQL' },
        ]
    };

    const data = billingData || mockBillingData;
    const spendingTrend = ((data.currentMonthSpend - data.previousMonthSpend) / data.previousMonthSpend) * 100;

    return (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
            <div className="mb-6">
                <h3 className="text-xl font-semibold text-gray-900 mb-2 flex items-center">
                    <DollarSign className="h-6 w-6 text-blue-600 mr-2" />
                    Billing Overview
                </h3>
            </div>

            {/* Key Metrics */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                <div className="bg-gray-50 rounded-lg p-4">
                    <div className="text-sm text-gray-500 mb-1">Current Month Spend</div>
                    <div className="text-2xl font-bold text-gray-900">${data.currentMonthSpend.toFixed(2)}</div>
                </div>
                <div className="bg-gray-50 rounded-lg p-4">
                    <div className="text-sm text-gray-500 mb-1">Previous Month</div>
                    <div className="text-2xl font-bold text-gray-900">${data.previousMonthSpend.toFixed(2)}</div>
                </div>
                <div className="bg-gray-50 rounded-lg p-4">
                    <div className="text-sm text-gray-500 mb-1">Projected Spend</div>
                    <div className="text-2xl font-bold text-gray-900">${data.projectedSpend.toFixed(2)}</div>
                </div>
                <div className="bg-gray-50 rounded-lg p-4">
                    <div className="text-sm text-gray-500 mb-1">Budget Limit</div>
                    <div className="text-2xl font-bold text-gray-900">${data.budgetLimit.toFixed(2)}</div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Top Services */}
                <div>
                    <h4 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
                        <TrendingUp className="h-5 w-5 text-blue-600 mr-2" />
                        Top Services by Cost
                    </h4>
                    <div className="space-y-3">
                        {data.topServices.map((service, index) => (
                            <div key={index} className="flex justify-between items-center">
                                <span className="text-gray-600">{service.name}</span>
                                <span className="font-medium">${service.cost.toFixed(2)}</span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Recent Charges */}
                <div>
                    <h4 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
                        <Calendar className="h-5 w-5 text-blue-600 mr-2" />
                        Recent Charges
                    </h4>
                    <div className="space-y-3">
                        {data.recentCharges.map((charge, index) => (
                            <div key={index} className="flex justify-between items-center">
                                <div>
                                    <div className="text-sm text-gray-600">{charge.service}</div>
                                    <div className="text-xs text-gray-500">{charge.date}</div>
                                </div>
                                <span className="font-medium">${charge.amount.toFixed(2)}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Budget Alert */}
            {data.projectedSpend > data.budgetLimit * 0.8 && (
                <div className="mt-6 bg-yellow-50 border-l-4 border-yellow-400 p-4">
                    <div className="flex items-center">
                        <AlertCircle className="h-5 w-5 text-yellow-400 mr-2" />
                        <div className="text-sm text-yellow-700">
                            Warning: Projected spend is approaching budget limit
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default BillingInfo;