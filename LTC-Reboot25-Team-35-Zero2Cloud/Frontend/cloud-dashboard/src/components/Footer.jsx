import React from 'react';

const Footer = () => {
  return (
    <footer className="bg-gray-50 border-t border-gray-200 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div className="flex items-center justify-between text-sm text-gray-500">
          <div>
            <p>&copy; 2025 Cloud Dashboard. All rights reserved.</p>
          </div>
          <div className="flex items-center space-x-4">
            <span>Version 1.2.3</span>
            <span>•</span>
            <span>Last updated: Jan 1, 2025</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;