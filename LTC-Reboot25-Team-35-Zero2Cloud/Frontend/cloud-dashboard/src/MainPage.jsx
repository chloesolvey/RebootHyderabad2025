
import React, { useState } from 'react';
import Header from './components/Header.jsx';
import Footer from './components/Footer.jsx';
import Navigation from './components/Navigation.jsx';
import ProvisioningTab from './components/Provisioning/ProvisioningTab.jsx';
import GovernanceTab from './components/Governance/GovernanceTab.jsx';
import MonitoringTab from './components/Monitoring/MonitoringTab.jsx';
import TeamRegistration from './components/Provisioning/TeamRegistration.jsx';
import { TabTypes } from './types/index.js';

function MainPage() {
    const [activeTab, setActiveTab] = useState(TabTypes.PROVISIONING);

    const renderActiveTab = () => {
        switch (activeTab) {
            case TabTypes.PROVISIONING:
                return <ProvisioningTab />;
            case TabTypes.GOVERNANCE:
                return <GovernanceTab />;
            case TabTypes.MONITORING:
                return <MonitoringTab />;
            default:
                return <ProvisioningTab />;
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex flex-col">
            <Header />

            {/* Team Registration Card */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <TeamRegistration />
            </div>

            {/* Navigation and Tab Content */}
            <Navigation activeTab={activeTab} onTabChange={setActiveTab} />
            <main className="flex-1">
                <div className="flex-1">
                    {renderActiveTab()}
                </div>
            </main>
            <Footer />
        </div>
    );
}

export default MainPage;