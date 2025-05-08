import React, { useState, useEffect } from 'react';
import { apiService } from '../config/api';
import UTIF from 'utif';

export function RecognizeFingerprint() {
    const [areas, setAreas] = useState([]);
    const [segmentationModels, setSegmentationModels] = useState([]);
    const [recognitionModels, setRecognitionModels] = useState([]);
    const [fingerprintFile, setFingerprintFile] = useState(null);
    const [previewImage, setPreviewImage] = useState(null);
    const [selectedArea, setSelectedArea] = useState('');
    const [selectedSegmentationModel, setSelectedSegmentationModel] = useState('');
    const [selectedRecognitionModel, setSelectedRecognitionModel] = useState('');
    const [selectedAccessType, setSelectedAccessType] = useState('ENTRY');
    const [recognitionResult, setRecognitionResult] = useState(null);
    const [error, setError] = useState(null);
    const [notification, setNotification] = useState("");

    const ACCESS_TYPES = [
        { value: 'ENTRY', label: 'Vào' },
        { value: 'EXIT', label: 'Ra' },
    ];

    useEffect(() => {
        const loadData = async () => {
            try {
                const [areasResponse, segModelsResponse, recModelsResponse] = await Promise.all([
                    apiService.getAreas(),
                    apiService.getSegmentationModels(),
                    apiService.getRecognitionModels()
                ]);

                setAreas(areasResponse);
                setSegmentationModels(segModelsResponse);
                setRecognitionModels(recModelsResponse);

                if (segModelsResponse.length > 0) {
                    setSelectedSegmentationModel(segModelsResponse[0].id);
                }
                if (recModelsResponse.length > 0) {
                    setSelectedRecognitionModel(recModelsResponse[0].id);
                }
            } catch (error) {
                setError('Lỗi tải dữ liệu');
            }
        };

        loadData();
    }, []);

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        setFingerprintFile(file);
    
        if (file.name.toLowerCase().endsWith('.tif') || file.name.toLowerCase().endsWith('.tiff')) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const buffer = e.target.result;
                const ifds = UTIF.decode(buffer);
                UTIF.decodeImage(buffer, ifds[0]);
                const rgba = UTIF.toRGBA8(ifds[0]);
                
                const canvas = document.createElement('canvas');
                canvas.width = ifds[0].width;
                canvas.height = ifds[0].height;
                const ctx = canvas.getContext('2d');
                
                const imgData = new ImageData(new Uint8ClampedArray(rgba), ifds[0].width, ifds[0].height);
                ctx.putImageData(imgData, 0, 0);
                
                setPreviewImage(canvas.toDataURL('image/png'));
            };
            reader.readAsArrayBuffer(file);
        } else {
            const reader = new FileReader();
            reader.onloadend = () => {
                setPreviewImage(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleRecognizeFingerprint = async (e) => {
        e.preventDefault();

        if (!fingerprintFile) {
            setError('Vui lòng chọn ảnh dấu vân tay');
            return;
        }
        if (!selectedArea) {
            setError('Vui lòng chọn khu vực');
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
            formData.append('areaId', selectedArea);
            formData.append('segmentationModelId', selectedSegmentationModel);
            formData.append('recognitionModelId', selectedRecognitionModel);
            formData.append('accessType', selectedAccessType);

            const response = await apiService.recognizeFingerprint(formData);

            setRecognitionResult(response);
            setError(null);
        } catch (error) {
            setError('Lỗi nhận dạng dấu vân tay');
            setRecognitionResult(null);
        }
    };

    useEffect(() => {
        if (recognitionResult) {
            if (!recognitionResult.employee) {
                setNotification("Không tìm thấy nhân viên nào khớp với dấu vân tay này");
            }
            else if (!recognitionResult.active) {
                setNotification("Dấu vân tay không hoạt động");
            } else if (!recognitionResult.accessable) {
                setNotification("Dấu vân tay không có quyền truy cập vào khu vực này");
            } else if (recognitionResult.authorized) {
                setNotification("Dấu vân tay đã được xác nhận thành công");
            } else {
                setNotification("Dấu vân tay không được xác nhận");
            }
        }
    }, [recognitionResult]);

    return (
        <div>
            <h2 className="text-2xl font-semibold mb-4">Nhận Dạng Dấu Vân Tay</h2>

            <form onSubmit={handleRecognizeFingerprint} className="space-y-4">
                <div>
                    <label className="block mb-2">Khu Vực</label>
                    <select
                        value={selectedArea}
                        onChange={(e) => setSelectedArea(e.target.value)}
                        className="w-full p-2 border rounded-md"
                    >
                        <option value="">Chọn khu vực</option>
                        {areas.map((area) => (
                            <option key={area.id} value={area.id}>
                                {area.name}
                            </option>
                        ))}
                    </select>
                </div>

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
                                {model.name.replace('_', '.') || model.id} {model.accuracy ? `(${(model.accuracy * 100).toFixed(2)}%)` : ''}
                            </option>
                        ))}
                    </select>
                </div>

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
                                {model.name.replace('_', '.') || model.id} {model.accuracy ? `(${(model.accuracy * 100).toFixed(2)}%)` : ''}
                            </option>
                        ))}
                    </select>
                </div>

                <div>
                    <label className="block mb-2">Loại Truy Cập</label>
                    <select
                        value={selectedAccessType}
                        onChange={(e) => setSelectedAccessType(e.target.value)}
                        className="w-full p-2 border rounded-md"
                    >
                        {ACCESS_TYPES.map((type) => (
                            <option key={type.value} value={type.value}>
                                {type.label}
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

                {previewImage && (
                    <div className="text-center">
                        <img
                            src={previewImage}
                            alt="Ảnh dấu vân tay"
                            className="max-h-[300px] mx-auto rounded-lg"
                        />
                    </div>
                )}

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                        {error}
                    </div>
                )}

                <button
                    type="submit"
                    className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 transition"
                >
                    Nhận Dạng Dấu Vân Tay
                </button>
            </form>

            {recognitionResult && (
                <div className="mt-6">
                    {recognitionResult.matched ? (
                        <div className={`
                            p-4 rounded-lg 
                            ${recognitionResult.authorized && recognitionResult.accessable && recognitionResult.active ? 'bg-green-50 border-green-200' : 'bg-yellow-50 border-yellow-200'}
                        `}>
                            <h3 className={`
                                text-lg font-semibold mb-2 
                                ${recognitionResult.authorized && recognitionResult.accessable && recognitionResult.active ? 'text-green-700' : 'text-yellow-700'}
                            `}>
                                {notification}
                            </h3>

                            <div className="space-y-2">
                                <div className="flex items-center">
                                    <img
                                        src={recognitionResult.employee?.photoUrl || '/avt.png'}
                                        alt="Ảnh nhân viên"
                                        className="w-20 h-20 rounded-full object-cover mr-4"
                                    />
                                    <div>
                                        <p className="font-semibold">
                                            {recognitionResult.employee?.fullName || 'Không xác định'}
                                        </p>
                                        <p>Mã NV: {recognitionResult.employee?.id || 'N/A'}</p>
                                    </div>
                                </div>

                                <p>Độ Chính Xác: {(recognitionResult.confidence * 100).toFixed(2)}%</p>
                                <p>Thời Gian: {recognitionResult.accessLog?.timestamp ? new Date(recognitionResult.accessLog?.timestamp).toLocaleString() : 'N/A'}</p>
                                <p>Khu Vực: {recognitionResult.accessLog?.area?.name || 'Không xác định'}</p>
                                <p>Loại Truy Cập: {selectedAccessType}</p>
                            </div>
                        </div>
                    ) : (
                        <div className="bg-red-50 border border-red-200 p-4 rounded-lg text-red-700">
                            <h3 className="text-lg font-semibold mb-2">Nhận Dạng Thất Bại</h3>
                            <p>Không tìm thấy dấu vân tay phù hợp</p>
                            <p>Độ Chính Xác: {(recognitionResult.confidence * 100).toFixed(2)}%</p>
                            <p>Thời Gian: {recognitionResult.accessLog?.timestamp ? new Date(recognitionResult.accessLog?.timestamp).toLocaleString() : 'N/A'}</p>
                            <p>Khu Vực: {recognitionResult.accessLog?.area?.name || 'Không xác định'}</p>
                            <p>Loại Truy Cập: {selectedAccessType}</p>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
