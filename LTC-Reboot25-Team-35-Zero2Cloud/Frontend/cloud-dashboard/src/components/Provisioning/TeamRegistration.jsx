import React, { useState } from 'react';
import { Users } from 'lucide-react';
import { toast } from 'react-toastify';
import TeamRegistrationModal from './TeamRegistrationModal';
import { registerTeam } from '../../services/teamService';

const TeamRegistrationForm = () => {
    const [formData, setFormData] = useState({
        teamName: '',
        tag: ''
    });
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    const validateForm = () => {
        if (!formData.teamName.trim()) {
            toast.error('Team name is required');
            return false;
        }

        if (!formData.tag.trim()) {
            toast.error('Team tag is required');
            return false;
        }

        const tagRegex = /^[A-Za-z0-9-]{2,10}$/;
        if (!tagRegex.test(formData.tag)) {
            toast.error('Tag must be 2-10 characters long and contain only uppercase letters, numbers, and hyphens');
            return false;
        }

        return true;
    };

    const resetForm = () => {
        setFormData({
            teamName: '',
            tag: ''
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        setIsLoading(true);
        try {
            await registerTeam('red-welder-466202-u6', formData.tag.toLowerCase());
            toast.success('Team registered successfully!');
            resetForm();
        } catch (error) {
            console.error('Team registration failed:', error);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-6 p-6">
            <div>
                <label htmlFor="teamName" className="block text-sm font-medium text-gray-700 mb-2">
                    Team Name
                </label>
                <input
                    type="text"
                    id="teamName"
                    name="teamName"
                    value={formData.teamName}
                    onChange={handleChange}
                    disabled={isLoading}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                    placeholder="Enter team name"
                />
            </div>

            <div>
                <label htmlFor="tag" className="block text-sm font-medium text-gray-700 mb-2">
                    Team Tag
                </label>
                <input
                    type="text"
                    id="tag"
                    name="tag"
                    value={formData.tag}
                    onChange={handleChange}
                    disabled={isLoading}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                    placeholder="Enter team tag (e.g., TEAM-1)"
                />
                <p className="mt-1 text-sm text-gray-500">
                    Tag must be 2-10 characters using uppercase letters, numbers, and hyphens only
                </p>
            </div>

            <button
                type="submit"
                disabled={isLoading}
                className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white transition-colors duration-200 ${
                    isLoading
                        ? 'bg-blue-400 cursor-not-allowed'
                        : 'bg-blue-600 hover:bg-blue-700'
                } focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500`}
            >
                {isLoading ? (
                    <span className="flex items-center">
                        <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Registering...
                    </span>
                ) : (
                    'Register Team'
                )}
            </button>
        </form>
    );
};

const TeamRegistration = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);

    return (
        <>
            <button
                onClick={() => setIsModalOpen(true)}
                className="w-full p-6 bg-white rounded-lg shadow-sm border border-gray-200 hover:bg-gray-50 transition-colors duration-200"
            >
                <div className="flex items-center space-x-4">
                    <Users className="h-8 w-8 text-blue-600" />
                    <div className="text-left">
                        <h3 className="text-lg font-semibold text-gray-900">Team Registration</h3>
                        <p className="text-sm text-gray-600">Register and manage your team members</p>
                    </div>
                </div>
            </button>

            <TeamRegistrationModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}>
                <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                    <div className="sm:flex sm:items-start">
                        <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 sm:mx-0 sm:h-10 sm:w-10">
                            <Users className="h-6 w-6 text-blue-600" />
                        </div>
                        <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                            <h3 className="text-lg leading-6 font-medium text-gray-900">
                                Team Registration
                            </h3>
                            <div className="mt-2">
                                <p className="text-sm text-gray-500">
                                    Register your team by providing a team name and tag.
                                </p>
                            </div>
                        </div>
                    </div>
                    <TeamRegistrationForm />
                </div>
            </TeamRegistrationModal>
        </>
    );
};

export default TeamRegistration;