// FingerprintManagement.jsx
import React, { useState, useEffect } from 'react';
import { apiService } from '../config/api';
import { formatDate, formatFingerprintPosition } from '../utils/formatters';

export function FingerprintManagement() {
  const [employees, setEmployees] = useState([]);
  const [employeeFingerprints, setEmployeeFingerprints] = useState({});
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [fingerprintSamples, setFingerprintSamples] = useState([]);
  const [maxSamples, setMaxSamples] = useState(0);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  // Tải danh sách nhân viên
  useEffect(() => {
    loadEmployees();
  }, []);

  // Tải dấu vân tay chi tiết khi chọn nhân viên
  useEffect(() => {
    if (selectedEmployee) {
      loadFingerprints(selectedEmployee.id);
    }
  }, [selectedEmployee]);

  // Tải danh sách nhân viên
  const loadEmployees = async () => {
    setLoading(true);
    try {
      const response = await apiService.getEmployees();
      setEmployees(response);

      // Tải sơ bộ thông tin vân tay cho mỗi nhân viên
      const fingerprintsData = {};
      for (const employee of response) {
        try {
          const fingerprints = await apiService.getEmployeeFingerprints(employee.id, true);
          fingerprintsData[employee.id] = fingerprints;
        } catch (error) {
          console.error(`Lỗi tải vân tay cho nhân viên ${employee.id}:`, error);
          fingerprintsData[employee.id] = [];
        }
      }
      setEmployeeFingerprints(fingerprintsData);
      setError(null);
    } catch (error) {
      console.error('Lỗi tải danh sách nhân viên:', error);
      setError('Lỗi tải danh sách nhân viên');
    } finally {
      setLoading(false);
    }
  };

  // Tải dấu vân tay chi tiết
  const loadFingerprints = async (employeeId) => {
    setLoading(true);
    try {
      const response = await apiService.getEmployeeFingerprints(employeeId, false);
      setFingerprintSamples(response);

      if (selectedEmployee) {
        setMaxSamples(selectedEmployee.maxNumberSamples);
      }

      setError(null);
    } catch (error) {
      console.error('Lỗi tải dấu vân tay:', error);
      setError('Lỗi tải dấu vân tay');
      setFingerprintSamples([]);
    } finally {
      setLoading(false);
    }
  };

  // Reload fingerprint data after operations
  const reloadFingerprintData = async () => {
    if (!selectedEmployee) return;

    // Reload fingerprint samples
    await loadFingerprints(selectedEmployee.id);

    // Update summary fingerprint data
    try {
      const summaryResponse = await apiService.getEmployeeFingerprints(selectedEmployee.id, true);
      setEmployeeFingerprints(prev => ({
        ...prev,
        [selectedEmployee.id]: summaryResponse
      }));
    } catch (error) {
      console.error('Lỗi tải tóm tắt vân tay:', error);
    }
  };

  // Handle employee selection
  const handleSelectEmployee = (employee) => {
    setSelectedEmployee(employee);
  };

  // Handle setting max fingerprint samples
  const handleSetMaxSamples = async (maxValue) => {
    if (!selectedEmployee) return;

    setLoading(true);
    try {
      await apiService.setMaxFingerprintSamples(selectedEmployee.id, maxValue);

      // Update employee data
      setEmployees(prevEmployees =>
        prevEmployees.map(emp =>
          emp.id === selectedEmployee.id
            ? { ...emp, maxNumberSamples: maxValue }
            : emp
        )
      );

      // Update selected employee
      setSelectedEmployee(prev => ({ ...prev, maxNumberSamples: maxValue }));
      setError(null);
    } catch (error) {
      console.error('Lỗi đặt số lượng dấu vân tay tối đa:', error);
      setError('Lỗi đặt số lượng dấu vân tay tối đa');
    } finally {
      setLoading(false);
    }
  };

  // Handle toggle fingerprint status
  const handleToggleFingerprintStatus = async (fingerprintId, enable) => {
    if (!selectedEmployee) return;

    setLoading(true);
    try {
      if (enable) {
        await apiService.enableFingerprint(fingerprintId);
      } else {
        await apiService.disableFingerprint(fingerprintId);
      }

      await reloadFingerprintData();
      setError(null);
    } catch (error) {
      console.error(`Lỗi ${enable ? 'kích hoạt' : 'vô hiệu hóa'} dấu vân tay:`, error);
      setError(`Lỗi ${enable ? 'kích hoạt' : 'vô hiệu hóa'} dấu vân tay`);
    } finally {
      setLoading(false);
    }
  };

  // Handle delete fingerprint
  const handleDeleteFingerprint = async (fingerprintId) => {
    if (!selectedEmployee) return;

    setLoading(true);
    try {
      await apiService.deleteFingerprint(fingerprintId);
      await reloadFingerprintData();
      setError(null);
    } catch (error) {
      console.error('Lỗi xóa dấu vân tay:', error);
      setError('Lỗi xóa dấu vân tay');
    } finally {
      setLoading(false);
    }
  };

  // Handle bulk operations
  const handleBulkOperation = async (operation) => {
    if (!selectedEmployee) return;

    setLoading(true);
    try {
      switch (operation) {
        case 'enableAll':
          await apiService.enableAllFingerprints(selectedEmployee.id);
          break;
        case 'disableAll':
          await apiService.disableAllFingerprints(selectedEmployee.id);
          break;
        case 'deleteAll':
          await apiService.deleteAllFingerprints(selectedEmployee.id);
          break;
        default:
          throw new Error('Thao tác không hợp lệ');
      }

      await reloadFingerprintData();
      setError(null);
    } catch (error) {
      console.error(`Lỗi thực hiện thao tác ${operation}:`, error);
      setError(`Lỗi thực hiện thao tác hàng loạt`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {/* Danh sách nhân viên */}
      <EmployeeList
        employees={employees}
        employeeFingerprints={employeeFingerprints}
        selectedEmployeeId={selectedEmployee?.id}
        onSelectEmployee={handleSelectEmployee}
        loading={loading && employees.length === 0}
      />

      {/* Chi tiết vân tay */}
      <div className="bg-white rounded-lg shadow-md">
        {selectedEmployee ? (
          <FingerprintDetails
            employee={selectedEmployee}
            fingerprintSamples={fingerprintSamples}
            maxSamples={maxSamples}
            onSetMaxSamples={handleSetMaxSamples}
            onToggleStatus={handleToggleFingerprintStatus}
            onDeleteFingerprint={handleDeleteFingerprint}
            onBulkOperation={handleBulkOperation}
            loading={loading}
          />
        ) : (
          <div className="p-4 text-center text-gray-500">
            Chọn nhân viên để quản lý dấu vân tay
          </div>
        )}

        {error && <ErrorMessage message={error} />}
      </div>
    </div>
  );
}

function EmployeeList({
  employees,
  employeeFingerprints,
  selectedEmployeeId,
  onSelectEmployee,
  loading
}) {
  return (
    <div className="bg-white rounded-lg shadow-md">
      <div className="p-4 border-b">
        <h2 className="text-lg font-semibold">Danh Sách Nhân Viên</h2>
      </div>
      <div className="max-h-[500px] overflow-y-auto">
        {loading ? (
          <LoadingIndicator message="Đang tải danh sách nhân viên..." />
        ) : (
          <table className="w-full">
            <thead className="sticky top-0 bg-gray-100">
              <tr>
                <th className="p-2 text-left">Ảnh</th>
                <th className="p-2 text-left">Mã NV</th>
                <th className="p-2 text-left">Tên</th>
                <th className="p-2 text-left">Dấu Vân Tay</th>
              </tr>
            </thead>
            <tbody>
              {employees.length > 0 ? (
                employees.map((employee) => {
                  const fingerprints = employeeFingerprints[employee.id] || [];
                  const activeFingerprints = fingerprints.filter(fp => fp.active).length;

                  return (
                    <tr
                      key={employee.id}
                      onClick={() => onSelectEmployee(employee)}
                      className={`cursor-pointer hover:bg-gray-100 ${selectedEmployeeId === employee.id ? 'bg-blue-50' : ''
                        }`}
                    >
                      <td className="p-2">
                        <img
                          src={employee.photoUrl || '/avt.png'}
                          alt={employee.fullName}
                          className="w-10 h-10 rounded-full object-cover"
                        />
                      </td>
                      <td className="p-2">{employee.id}</td>
                      <td className="p-2">{employee.fullName}</td>
                      <td className="p-2">
                        <div>{activeFingerprints} / {fingerprints.length} / {employee.maxNumberSamples}</div>
                        <div className="text-xs text-gray-400 italic">
                          (Hoạt động / Đã đăng ký / Giới hạn tối đa)
                        </div>
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan="4" className="p-4 text-center text-gray-500">
                    Không có nhân viên nào
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

// components/FingerprintDetails.jsx
function FingerprintDetails({
  employee,
  fingerprintSamples,
  maxSamples,
  onSetMaxSamples,
  onToggleStatus,
  onDeleteFingerprint,
  onBulkOperation,
  loading
}) {
  const [localMaxSamples, setLocalMaxSamples] = useState(maxSamples);

  // Update local state when prop changes
  React.useEffect(() => {
    setLocalMaxSamples(maxSamples);
  }, [maxSamples]);

  const hasActiveSamples = fingerprintSamples.some(sample => sample.active);

  return (
    <>
      <div className="p-4 border-b flex items-center">
        <img
          src={employee.photoUrl || '/avt.png'}
          alt={employee.fullName}
          className="w-16 h-16 rounded-full object-cover mr-4"
        />
        <div>
          <h3 className="text-lg font-semibold">{employee.fullName}</h3>
          <p className="text-gray-600">Mã NV: {employee.id}</p>
          <p className="text-sm text-gray-500">
            Dấu vân tay: {fingerprintSamples.length} / {employee.maxNumberSamples}
          </p>
          <p className="text-xs text-gray-400 italic mt-1">
            (Đã đăng ký / Giới hạn tối đa)
          </p>
        </div>
      </div>

      <div className="p-4">
        <div className="flex mb-4 space-x-2">
          <input
            type="number"
            value={localMaxSamples}
            onChange={(e) => setLocalMaxSamples(Number(e.target.value))}
            min="1"
            className="flex-grow p-2 border rounded"
            placeholder="Số lượng dấu vân tay tối đa"
            disabled={loading}
          />
          <button
            onClick={() => onSetMaxSamples(localMaxSamples)}
            className="bg-blue-500 text-white px-4 py-2 rounded disabled:bg-blue-300"
            disabled={loading}
          >
            Đặt Giới Hạn
          </button>
          {hasActiveSamples ? (
            <button
              onClick={() => onBulkOperation('disableAll')}
              className="bg-yellow-500 text-white px-4 py-2 rounded disabled:bg-yellow-300"
              disabled={loading}
            >
              Vô Hiệu Hóa Tất Cả
            </button>
          ) : (
            <button
              onClick={() => onBulkOperation('enableAll')}
              className="bg-green-500 text-white px-4 py-2 rounded disabled:bg-green-300"
              disabled={loading || fingerprintSamples.length === 0}
            >
              Kích Hoạt Tất Cả
            </button>
          )}
        </div>

        <h4 className="text-lg font-semibold mb-2">Danh Sách Dấu Vân Tay</h4>

        {loading && fingerprintSamples.length === 0 ? (
          <LoadingIndicator message="Đang tải dữ liệu vân tay..." />
        ) : (
          <>
            {fingerprintSamples.length > 0 ? (
              <>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {fingerprintSamples.map((sample) => (
                    <FingerprintCard
                      key={sample.id}
                      sample={sample}
                      onToggleStatus={onToggleStatus}
                      onDeleteFingerprint={onDeleteFingerprint}
                      loading={loading}
                    />
                  ))}
                </div>
                <div className="flex justify-end mt-4">
                  <button
                    onClick={() => onBulkOperation('deleteAll')}
                    className="bg-red-500 text-white px-4 py-2 rounded disabled:bg-red-300"
                    disabled={loading}
                  >
                    Xóa Tất Cả
                  </button>
                </div>
              </>
            ) : (
              <div className="text-center text-gray-500 py-4">
                Không có dấu vân tay nào được đăng ký
              </div>
            )}
          </>
        )}
      </div>
    </>
  );
}

function FingerprintCard({
  sample,
  onToggleStatus,
  onDeleteFingerprint,
  loading
}) {
  return (
    <div
      className={`border rounded-lg p-4 ${sample.active
          ? 'border-green-500 bg-green-50'
          : 'border-gray-300 bg-gray-50 opacity-70'
        }`}
    >
      <div className="flex justify-between items-center mb-2">
        <h5 className="font-semibold">
          {formatFingerprintPosition(sample.position)}
        </h5>
        <span
          className={`px-2 py-1 rounded text-xs ${sample.active
              ? 'bg-green-500 text-white'
              : 'bg-gray-300 text-gray-700'
            }`}
        >
          {sample.active ? 'Hoạt Động' : 'Ngừng Hoạt Động'}
        </span>
      </div>
      <div className="text-sm text-gray-600 mb-2">
        <p>Mã: {sample.id.substring(0, 8)}...</p>
        <p>Ngày Đăng Ký: {formatDate(sample.capturedAt)}</p>
        {sample.quality && (
          <p>Chất Lượng: {(sample.quality * 100).toFixed(2)}%</p>
        )}
      </div>
      <div className="flex space-x-2">
        {sample.active ? (
          <button
            onClick={() => onToggleStatus(sample.id, false)}
            className="flex-grow bg-yellow-500 text-white px-2 py-1 rounded text-sm disabled:bg-yellow-300"
            disabled={loading}
          >
            Vô Hiệu Hóa
          </button>
        ) : (
          <button
            onClick={() => onToggleStatus(sample.id, true)}
            className="flex-grow bg-green-500 text-white px-2 py-1 rounded text-sm disabled:bg-green-300"
            disabled={loading}
          >
            Kích Hoạt
          </button>
        )}
        <button
          onClick={() => onDeleteFingerprint(sample.id)}
          className="bg-red-500 text-white px-2 py-1 rounded text-sm disabled:bg-red-300"
          disabled={loading}
        >
          Xóa
        </button>
      </div>
    </div>
  );
}

// components/LoadingIndicator.jsx
function LoadingIndicator({ message = 'Đang tải dữ liệu...' }) {
  return (
    <div className="p-4 text-center text-gray-500">
      {message}
    </div>
  );
}

function ErrorMessage({ message }) {
  return (
    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mx-4 mb-4">
      {message}
    </div>
  );
}