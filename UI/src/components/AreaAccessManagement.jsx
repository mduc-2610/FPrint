import React, { useState, useEffect } from 'react';
import { apiService } from '../config/api';
import { formatDate } from '../utils/formatters';

export function AreaAccessManagement() {
    const [employees, setEmployees] = useState([]);
    const [selectedEmployee, setSelectedEmployee] = useState(null);
    const [areas, setAreas] = useState([]);
    const [accessPermissions, setAccessPermissions] = useState([]);
    const [selectedArea, setSelectedArea] = useState('');
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadInitialData = async () => {
            try {
                const [employeesResponse, areasResponse] = await Promise.all([
                    apiService.getEmployees(),
                    apiService.getAreas()
                ]);
                setEmployees(employeesResponse);
                setAreas(areasResponse);
            } catch (error) {
                setError('Lỗi tải dữ liệu');
            }
        };

        loadInitialData();
    }, []);

    useEffect(() => {
        const loadEmployeeAccess = async () => {
            if (selectedEmployee) {
                try {
                    const response = await apiService.getEmployeeAccess(selectedEmployee.id);
                    setAccessPermissions(response);
                } catch (error) {
                    setError('Lỗi tải quyền truy cập');
                }
            }
        };

        loadEmployeeAccess();
    }, [selectedEmployee]);

    const handleGrantAccess = async () => {
        if (!selectedEmployee || !selectedArea) {
            setError('Vui lòng chọn nhân viên và khu vực');
            return;
        }

        try {
            await apiService.grantAccess(selectedEmployee.id, selectedArea);

            const response = await apiService.getEmployeeAccess(selectedEmployee.id);
            setAccessPermissions(response);

            setSelectedArea('');
            setError(null);
        } catch (error) {
            setError('Lỗi cấp quyền truy cập');
        }
    };
    const handleGrantAccessForAllAreas = async () => {
        if (!selectedEmployee) {
            setError('Vui lòng chọn nhân viên');
            return;
        }

        try {
            await apiService.grantAccessForAllArea(selectedEmployee.id, selectedArea);

            const response = await apiService.getEmployeeAccess(selectedEmployee.id);
            setAccessPermissions(response);

            setSelectedArea('');
            setError(null);
        } catch (error) {
            setError('Lỗi cấp quyền truy cập');
        }
    };

    const handleRevokeAccess = async (accessId) => {
        try {
            await apiService.revokeAccess(accessId);

            if (selectedEmployee) {
                const response = await apiService.getEmployeeAccess(selectedEmployee.id);
                setAccessPermissions(response);
                setError(null);
            }
        } catch (error) {
            setError('Lỗi thu hồi quyền truy cập');
        }
    };

    const handleRevokeAllAccess = async () => {
        try {
            const revokePromises = accessPermissions.map((access) =>
                apiService.revokeAccess(access.id)
            );
            await Promise.all(revokePromises);

            if (selectedEmployee) {
                const response = await apiService.getEmployeeAccess(selectedEmployee.id);
                setAccessPermissions(response);
                setError(null);
            }

        } catch (error) {
            setError('Lỗi thu hồi quyền truy cập');
        }
    };

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <EmployeeTable
                employees={employees}
                selectedEmployee={selectedEmployee}
                onSelectEmployee={setSelectedEmployee}
            />

            <EmployeeAccessPanel
                selectedEmployee={selectedEmployee}
                areas={areas}
                accessPermissions={accessPermissions}
                selectedArea={selectedArea}
                setSelectedArea={setSelectedArea}
                handleGrantAccess={handleGrantAccess}
                handleGrantAccessForAllAreas={handleGrantAccessForAllAreas}
                handleRevokeAccess={handleRevokeAccess}
                handleRevokeAllAccess={handleRevokeAllAccess}
                error={error}
            />
        </div>
    );
}

function EmployeeTable({ employees, selectedEmployee, onSelectEmployee }) {
    return (
        <div className="bg-white rounded-lg shadow-md">
            <div className="p-4 border-b">
                <h2 className="text-lg font-semibold">Danh Sách Nhân Viên</h2>
            </div>
            <div className="max-h-[500px] overflow-y-auto">
                <table className="w-full">
                    <thead className="sticky top-0 bg-gray-100">
                        <tr>
                            <th className="p-2 text-left">Ảnh</th>
                            <th className="p-2 text-left">Mã NV</th>
                            <th className="p-2 text-left">Tên</th>
                        </tr>
                    </thead>
                    <tbody>
                        {employees.map((employee) => (
                            <tr
                                key={employee.id}
                                onClick={() => onSelectEmployee(employee)}
                                className={`cursor-pointer hover:bg-gray-100 ${selectedEmployee?.id === employee.id ? 'bg-blue-50' : ''
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
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

function EmployeeAccessPanel({
    selectedEmployee,
    areas,
    accessPermissions,
    selectedArea,
    setSelectedArea,
    handleGrantAccess,
    handleGrantAccessForAllAreas,
    handleRevokeAccess,
    handleRevokeAllAccess,
    error
}) {
    if (!selectedEmployee) {
        return (
            <div className="bg-white rounded-lg shadow-md">
                <div className="p-4 text-center text-gray-500">
                    Chọn nhân viên để quản lý quyền truy cập
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-lg shadow-md">
            <div className="p-4 border-b flex items-center">
                <img
                    src={selectedEmployee.photoUrl || '/avt.png'}
                    alt={selectedEmployee.fullName}
                    className="w-16 h-16 rounded-full object-cover mr-4"
                />
                <div>
                    <h3 className="text-lg font-semibold">{selectedEmployee.fullName}</h3>
                    <p className="text-gray-600">Mã NV: {selectedEmployee.id}</p>
                </div>
            </div>

            <div className="p-4">
                <AreaSelector
                    areas={areas}
                    selectedArea={selectedArea}
                    onAreaChange={setSelectedArea}
                    onGrantAccess={handleGrantAccess}
                    onGrantAllAccess={handleGrantAccessForAllAreas}
                />

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                        {error}
                    </div>
                )}

                <h4 className="text-lg font-semibold mb-2">Quyền Truy Cập Hiện Tại</h4>

                <AccessPermissionsTable
                    accessPermissions={accessPermissions}
                    onRevokeAccess={handleRevokeAccess}
                    onRevokeAllAccess={handleRevokeAllAccess}
                />
            </div>
        </div>
    );
}

function AreaSelector({
    areas,
    selectedArea,
    onAreaChange,
    onGrantAccess,
    onGrantAllAccess
}) {
    return (
        <div className="flex mb-4">
            <select
                value={selectedArea}
                onChange={(e) => onAreaChange(e.target.value)}
                className="flex-grow mr-2 p-2 border rounded"
            >
                <option value="">Chọn khu vực</option>
                {areas.map((area) => (
                    <option key={area.id} value={area.id}>
                        {area.name}
                    </option>
                ))}
            </select>
            <div style={{ display: 'flex', gap: '10px' }}>
                <button
                    onClick={onGrantAccess}
                    className="bg-green-500 text-white px-4 py-2 rounded"
                >
                    Cấp Quyền
                </button>
                <button
                    onClick={onGrantAllAccess}
                    className="bg-green-500 text-white px-4 py-2 rounded"
                >
                    Cấp Quyền cho tất cả khu vực
                </button>
            </div>
        </div>
    );
}

function AccessPermissionsTable({
    accessPermissions,
    onRevokeAccess,
    onRevokeAllAccess
}) {
    return (
        <>
            <div className="max-h-[300px] overflow-y-auto">
                <table className="w-full">
                    <thead className="bg-gray-100">
                        <tr>
                            <th className="p-2 text-left">Khu Vực</th>
                            <th className="p-2 text-left">Ngày Cấp</th>
                            <th className="p-2 text-right">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        {accessPermissions.map((access) => (
                            <tr key={access.id} className="border-b">
                                <td className="p-2">{access.area.name}</td>
                                <td className="p-2">{formatDate(access.timestamp)}</td>
                                <td className="p-2 text-right">
                                    <button
                                        onClick={() => onRevokeAccess(access.id)}
                                        className="text-red-500 hover:text-red-700"
                                    >
                                        Thu Hồi
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {accessPermissions.length > 0 && (
                <div className="flex justify-end mt-4">
                    <button
                        onClick={onRevokeAllAccess}
                        className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
                    >
                        Thu Hồi toàn bộ quyền truy cập
                    </button>
                </div>
            )}
        </>
    );
}