import React, { useState, useEffect } from 'react';
import { apiService } from '../config/api';

export function RegisterFingerprint() {
  const [employees, setEmployees] = useState([]);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [fingerprintFile, setFingerprintFile] = useState(null);
  const [fingerprintPosition, setFingerprintPosition] = useState('');
  const [previewImage, setPreviewImage] = useState(null);
  const [registrationResult, setRegistrationResult] = useState(null);
  const [error, setError] = useState(null);

  const [segmentationModels, setSegmentationModels] = useState([]);
  const [recognitionModels, setRecognitionModels] = useState([]);
  const [selectedSegmentationModel, setSelectedSegmentationModel] = useState('');
  const [selectedRecognitionModel, setSelectedRecognitionModel] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const employeesResponse = await apiService.getEmployees();
        setEmployees(employeesResponse);

        const segmentationModelsResponse = await apiService.getSegmentationModels();
        setSegmentationModels(segmentationModelsResponse);

        const recognitionModelsResponse = await apiService.getRecognitionModels();
        setRecognitionModels(recognitionModelsResponse);
      } catch (error) {
        setError('Lỗi tải dữ liệu');
      }
    };

    loadData();
  }, []);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setFingerprintFile(file);

    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewImage(reader.result);
    };
    reader.readAsDataURL(file);
  };

  const handleRegisterFingerprint = async (e) => {
    e.preventDefault();

    if (!selectedEmployee) {
      setError('Vui lòng chọn nhân viên');
      return;
    }
    if (!fingerprintFile) {
      setError('Vui lòng chọn ảnh dấu vân tay');
      return;
    }
    if (!fingerprintPosition) {
      setError('Vui lòng chọn vị trí dấu vân tay');
      return;
    }
    if (!selectedSegmentationModel) {
      setError('Vui lòng chọn mô hình phân đoạn');
      return;
    }
    if (!selectedRecognitionModel) {
      setError('Vui lòng chọn mô hình nhận dạng');
      return;
    }

    try {
      const formData = new FormData();
      formData.append('file', fingerprintFile);
      formData.append('position', fingerprintPosition);
      formData.append('employeeId', selectedEmployee.id);
      formData.append('segmentationModelId', selectedSegmentationModel);
      formData.append('recognitionModelId', selectedRecognitionModel);

      const response = await apiService.registerFingerprint(selectedEmployee.id, formData);

      setRegistrationResult(response);
      setError(null);
    } catch (error) {
      setError('Lỗi đăng ký dấu vân tay');
      setRegistrationResult(null);
    }
  };

  return (
    <div>
      <h2 className="text-2xl font-semibold mb-4">Đăng Ký Dấu Vân Tay Mới</h2>

      {/* Chọn nhân viên */}
      <div className="mb-4">
        <label className="block mb-2">Chọn Nhân Viên</label>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 max-h-[300px] overflow-y-auto">
          {employees.map((employee) => (
            <div
              key={employee.id}
              onClick={() => setSelectedEmployee(employee)}
              className={`p-4 border rounded-lg cursor-pointer transition-colors ${selectedEmployee?.id === employee.id
                ? 'bg-blue-50 border-blue-500'
                : 'hover:bg-gray-50'
                }`}
            >
              <div className="flex items-center">
                <img
                  src={employee.photoUrl || '/avt.png'}
                  alt={employee.fullName}
                  className="w-16 h-16 rounded-full object-cover mr-4"
                />
                <div>
                  <h3 className="font-semibold">{employee.fullName}</h3>
                  <p className="text-gray-600">Mã NV: {employee.id}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Thông tin nhân viên được chọn */}
      {selectedEmployee && (
        <div className="bg-white border rounded-lg p-4 mb-4">
          <div className="flex items-center">
            <img
              src={selectedEmployee.photoUrl || '/avt.png'}
              alt={selectedEmployee.fullName}
              className="w-20 h-20 rounded-full object-cover mr-6"
            />
            <div>
              <h3 className="text-xl font-semibold">{selectedEmployee.fullName}</h3>
              <p>Mã NV: {selectedEmployee.id}</p>
              <p>SĐT: {selectedEmployee.phoneNumber || 'Chưa cập nhật'}</p>
            </div>
          </div>
        </div>
      )}

      {/* Form đăng ký dấu vân tay */}
      <form onSubmit={handleRegisterFingerprint} className="space-y-4">
        <div>
          <label className="block mb-2">Vị Trí Dấu Vân Tay</label>
          <select
            value={fingerprintPosition}
            onChange={(e) => setFingerprintPosition(e.target.value)}
            className="w-full p-2 border rounded-md"
          >
            <option value="">Chọn vị trí</option>
            {[
              'LEFT_THUMB', 'LEFT_INDEX', 'LEFT_MIDDLE', 'LEFT_RING', 'LEFT_LITTLE',
              'RIGHT_THUMB', 'RIGHT_INDEX', 'RIGHT_MIDDLE', 'RIGHT_RING', 'RIGHT_LITTLE'
            ].map((pos) => (
              <option key={pos} value={pos}>
                {pos.replace('_', ' ').replace(/\b\w/g, letter => letter.toUpperCase())}
              </option>
            ))}
          </select>
        </div>

        {/* Chọn mô hình phân đoạn */}
        <div>
          <label className="block mb-2">Mô Hình Phân Đoạn</label>
          <select
            value={selectedSegmentationModel}
            onChange={(e) => setSelectedSegmentationModel(e.target.value)}
            className="w-full p-2 border rounded-md"
          >
            <option value="">Chọn mô hình phân đoạn</option>
            {segmentationModels.map((model) => (
              <option key={model.id} value={model.id}>
                {model.name} (Ver. {model.version})
              </option>
            ))}
          </select>
        </div>

        {/* Chọn mô hình nhận dạng */}
        <div>
          <label className="block mb-2">Mô Hình Nhận Dạng</label>
          <select
            value={selectedRecognitionModel}
            onChange={(e) => setSelectedRecognitionModel(e.target.value)}
            className="w-full p-2 border rounded-md"
          >
            <option value="">Chọn mô hình nhận dạng</option>
            {recognitionModels.map((model) => (
              <option key={model.id} value={model.id}>
                {model.name} (Ver. {model.version})
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block mb-2">Ảnh Dấu Vân Tay</label>
          <input
            type="file"
            accept=".bmp,.tif,.tiff"
            onChange={handleFileChange}
            className="w-full p-2 border rounded-md"
          />
        </div>

        {/* Xem trước ảnh */}
        {previewImage && (
          <div className="text-center">
            <img
              src={previewImage}
              alt="Ảnh dấu vân tay"
              className="max-h-[300px] mx-auto rounded-lg"
            />
          </div>
        )}

        {/* Hiển thị lỗi */}
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}

        <button
          type="submit"
          className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 transition"
        >
          Đăng Ký Dấu Vân Tay
        </button>
      </form>

      {/* Kết quả đăng ký */}
      {registrationResult?.error ? (
        <div className="mt-6 bg-red-50 border border-red-200 p-4 rounded-lg">
          <h3 className="text-lg font-semibold text-red-700 mb-2">
            Đăng Ký Thất Bại
          </h3>
          <p>{registrationResult.error}</p>
        </div>
      ) : registrationResult ? (
        <div className="mt-6 bg-green-50 border border-green-200 p-4 rounded-lg">
          <h3 className="text-lg font-semibold text-green-700 mb-2">
            Đăng Ký Thành Công
          </h3>
          <div className="space-y-2">
            <p>Nhân Viên: {selectedEmployee.fullName}</p>
            <p>Vị Trí: {fingerprintPosition.replace('_', ' ')}</p>
            <p>Chất Lượng: {(registrationResult.quality * 100).toFixed(2)}%</p>
            <p>Ngày Đăng Ký: {new Date(registrationResult.capturedAt).toLocaleString()}</p>
          </div>
        </div>
      ) : null}
    </div>
  );
}