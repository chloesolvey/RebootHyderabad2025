
import React from 'react';
import { X } from 'lucide-react';

const TeamRegistrationModal = ({ isOpen, onClose, children }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 overflow-y-auto">
            <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
                <div className="fixed inset-0 transition-opacity" aria-hidden="true">
                    <div className="absolute inset-0 bg-gray-500 opacity-75" onClick={onClose}></div>
                </div>

                <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
                    <div className="absolute top-0 right-0 pt-4 pr-4">
                        <button
                            onClick={onClose}
                            className="bg-white rounded-md text-gray-400 hover:text-gray-500 focus:outline-none"
                        >
                            <X className="h-6 w-6" />
                        </button>
                    </div>
                    {children}
                </div>
            </div>
        </div>
    );
};

export default TeamRegistrationModal;