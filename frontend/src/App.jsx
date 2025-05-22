import React from 'react';
import { AreaAccessManagement } from './components/AreaAccessManagement';
import { RecognizeFingerprint } from './components/RecognizeFingerprint';
import { EmployeeStatistics } from './components/EmployeeStatistics';

function App() {
  const [activeModule, setActiveModule] = React.useState('employee-access');

  const renderModule = () => {
    switch (activeModule) {
      case 'employee-access':
        return <AreaAccessManagement />;
      case 'recognize-fingerprint':
        return <RecognizeFingerprint />;
      case 'employee-statistics':
        return <EmployeeStatistics />;
      default:
        return <AreaAccessManagement />;
    }
  };

  const modules = [
    { key: 'employee-access', label: 'Cấp Quyền Truy Cập' },
    { key: 'recognize-fingerprint', label: 'Nhận Dạng Dấu Vân Tay' },
    { key: 'employee-statistics', label: 'Thống Kê Nhân Viên' }
  ];

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold text-center mb-6 text-green-800">
        Hệ thống quản lý nhận diện vân tay nhân viên
      </h1>
      
      <div className="flex flex-wrap justify-center gap-2 mb-6">
        {modules.map((module) => (
          <button
            key={module.key}
            onClick={() => setActiveModule(module.key)}
            className={`
              px-4 py-2 rounded-md transition-colors 
              ${activeModule === module.key 
                ? 'bg-blue-600 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }
            `}
          >
            {module.label}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-lg shadow-md p-6">
        {renderModule()}
      </div>
    </div>
  );
}

export default App;