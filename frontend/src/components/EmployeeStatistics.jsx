import React, { useState, useEffect } from 'react';
import { apiService } from '../config/api';
export function EmployeeStatistics() {
    const [dateRange, setDateRange] = useState({
        startDate: '',
        endDate: ''
    });
    const [employees, setEmployees] = useState([]);
    const [selectedEmployee, setSelectedEmployee] = useState(null);
    const [accessLogs, setAccessLogs] = useState([]);
    const [filteredLogs, setFilteredLogs] = useState([]);
    const [filters, setFilters] = useState({ accessType: '', areaId: '' });
    const [error, setError] = useState(null);
    const [areas, setAreas] = useState([]);

    useEffect(() => {
        const endDate = new Date();
        const startDate = new Date();
        startDate.setDate(startDate.getDate() - 30);

        setDateRange({
            startDate: formatDateForInput(startDate),
            endDate: formatDateForInput(endDate)
        });

        loadAreas();
    }, []);

    useEffect(() => {
        if (selectedEmployee) {
            handleFilterLogs();
        }
    }, [filters]);

    useEffect(() => {
        setFilteredLogs(accessLogs);
    }, [accessLogs]);

    const formatDateForInput = (date) => {
        const pad = (num) => num.toString().padStart(2, '0');
        return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
    };

    const loadAreas = async () => {
        try {
            const response = await apiService.getAreas();
            setAreas(response);
        } catch (error) {
            setError('Lỗi tải danh sách khu vực');
        }
    };

    

    const loadEmployeeStatistics = async () => {
        try {
            const response = await apiService.getEmployeeStatistics(
                dateRange.startDate,
                dateRange.endDate
            );
            setEmployees(response);
            setSelectedEmployee(null);
            setAccessLogs([]);
            setFilteredLogs([]);
            setError(null);
        } catch (error) {
            setError('Lỗi tải thống kê nhân viên');
        }
    };

    const handleEmployeeSelect = async (employeeId) => {
        try {
            const response = await apiService.getEmployeeAccessLogs(
                employeeId,
                dateRange.startDate,
                dateRange.endDate
            );

            const emp = employees.find(emp => emp.employeeId === employeeId);
            setSelectedEmployee(emp);
            setAccessLogs(response);
            setFilters({ accessType: '', areaId: '' });
            setError(null);
        } catch (error) {
            setError('Lỗi tải nhật ký truy cập');
        }
    };

    const handleFilterLogs = async () => {
        try {
            const logs = await apiService.getEmployeeAccessLogs(
                selectedEmployee.employeeId,
                dateRange.startDate,
                dateRange.endDate,
                filters.accessType || null,
                filters.areaId || null
            );
            setFilteredLogs(logs);
        } catch (error) {
            console.error("Lỗi tải nhật ký truy cập theo bộ lọc");
        }
    };

    const handleDateChange = (field, value) => {
        setDateRange(prev => ({ ...prev, [field]: value }));
    };

    const handleFilterChange = (field, value) => {
        setFilters(prev => ({ ...prev, [field]: value }));
    };

    return (
        <div className="container mx-auto p-4">
            <h2 className="text-2xl font-semibold mb-4">Thống Kê Nhân Viên</h2>

            <DateFilter
                startDate={dateRange.startDate}
                endDate={dateRange.endDate}
                onDateChange={handleDateChange}
                onSubmit={loadEmployeeStatistics}
            />

            {error && <ErrorMessage message={error} />}

            <EmployeeList
                employees={employees}
                selectedEmployeeId={selectedEmployee?.employeeId}
                onEmployeeSelect={handleEmployeeSelect}
            />

            {selectedEmployee && (
                <AccessLogDetails
                    employeeName={selectedEmployee.fullName}
                    accessLogs={filteredLogs}
                    areas={areas}
                    filters={filters}
                    onFilterChange={handleFilterChange}
                />
            )}
        </div>
    );
}


function DateFilter({ startDate, endDate, onDateChange, onSubmit }) {
    return (
        <div className="grid md:grid-cols-3 gap-4 mb-6">
            <div>
                <label className="block mb-2">Ngày Bắt Đầu</label>
                <input
                    type="datetime-local"
                    value={startDate}
                    onChange={(e) => onDateChange('startDate', e.target.value)}
                    className="w-full p-2 border rounded-md"
                />
            </div>
            <div>
                <label className="block mb-2">Ngày Kết Thúc</label>
                <input
                    type="datetime-local"
                    value={endDate}
                    onChange={(e) => onDateChange('endDate', e.target.value)}
                    className="w-full p-2 border rounded-md"
                />
            </div>
            <div className="flex items-end">
                <button
                    onClick={onSubmit}
                    className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 transition"
                >
                    Tải Thống Kê
                </button>
            </div>
        </div>
    );
}

function ErrorMessage({ message }) {
    return (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {message}
        </div>
    );
}

function EmployeeList({ employees, selectedEmployeeId, onEmployeeSelect }) {
    return (
        <div className="bg-white rounded-lg shadow-md">
            <div className="p-4 border-b">
                <h3 className="text-lg font-semibold">Danh Sách Nhân Viên</h3>
            </div>
            <div className="overflow-x-auto">
                <table className="w-full">
                    <thead className="bg-gray-100">
                        <tr>
                            <th className="p-2 text-left">Ảnh</th>
                            <th className="p-2 text-left">Mã NV</th>
                            <th className="p-2 text-left">Tên</th>
                            <th className="p-2 text-left">Tổng Truy Cập</th>
                        </tr>
                    </thead>
                    <tbody>
                        {employees.length > 0 ? (
                            employees.map((employee) => (
                                <tr
                                    key={employee.employeeId}
                                    onClick={() => onEmployeeSelect(employee.employeeId)}
                                    className={`cursor-pointer hover:bg-gray-50 ${selectedEmployeeId === employee.employeeId ? 'bg-blue-50' : ''
                                        }`}
                                >
                                    <td className="p-2">
                                        <img
                                            src={employee.photoUrl || '/avt.png'}
                                            alt={employee.fullName}
                                            className="w-10 h-10 rounded-full object-cover"
                                        />
                                    </td>
                                    <td className="p-2">{employee.employeeId}</td>
                                    <td className="p-2">{employee.fullName}</td>
                                    <td className="p-2">{employee.totalAccesses}</td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="4" className="p-4 text-center text-gray-500">
                                    Không có dữ liệu
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

function AccessLogDetails({ employeeName, accessLogs, areas, filters, onFilterChange }) {
    const formatDateTime = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleString('vi-VN', {
            weekday: 'short',
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };
    return (
        <div className="mt-6 bg-white rounded-lg shadow-md">
            <div className="p-4 border-b flex justify-between items-center">
                <h3 className="text-lg font-semibold">
                    Nhật Ký Truy Cập - {employeeName}
                </h3>
                <div className="flex space-x-2">
                    <select
                        value={filters.accessType}
                        onChange={(e) => onFilterChange('accessType', e.target.value)}
                        className="p-2 border rounded-md"
                    >
                        <option value="">Loại Truy Cập</option>
                        <option value="ENTRY">ENTRY</option>
                        <option value="EXIT">EXIT</option>
                    </select>
                    <select
                        value={filters.areaId}
                        onChange={(e) => onFilterChange('areaId', e.target.value)}
                        className="p-2 border rounded-md"
                    >
                        <option value="">Khu Vực</option>
                        {areas.map((area) => (
                            <option key={area.id} value={area.id}>
                                {area.name}
                            </option>
                        ))}
                    </select>
                </div>
            </div>
            <div className="overflow-x-auto">
                <table className="w-full">
                    <thead className="bg-gray-100">
                        <tr>
                            <th className="p-2 text-left">Thời Gian</th>
                            <th className="p-2 text-left">Loại Truy Cập</th>
                            <th className="p-2 text-left">Khu Vực</th>
                        </tr>
                    </thead>
                    <tbody>
                        {accessLogs.length > 0 ? (
                            accessLogs.map((log, index) => (
                                <tr
                                    key={index}
                                    className={`${log.authorized ? 'bg-green-50' : 'bg-red-50'} hover:bg-opacity-75`}
                                >
                                    <td className="p-2">{formatDateTime(log.timestamp)}</td>
                                    <td className="p-2">{log.accessType}</td>
                                    <td className="p-2">{log.area?.name || 'Chung'}</td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="3" className="p-4 text-center text-gray-500">
                                    Không có nhật ký truy cập
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
