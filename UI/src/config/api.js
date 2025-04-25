// Basic API configuration
const BASE_URL = "http://localhost:8080/api";
const recognitionModelId = "00a14633-b05b-47ef-9990-db7a587398e4"; // Default segmentation model ID
const segmentationModelId = "76bf4d29-becf-4297-842c-de2cec87a96d"; // Default recognition model ID

// API utility functions
export const apiService = {
  // Helper method to handle fetch responses
  async fetchJson(url, options = {}) {
    const response = await fetch(url, {
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! status: ${response.status}`
      );
    }

    return response.json();
  },

  // Employee-related methods
  getEmployees() {
    return fetch(`${BASE_URL}/employee`).then((response) => response.json());
  },

  // Area-related methods
  getAreas() {
    return fetch(`${BASE_URL}/area`).then((response) => response.json());
  },

  // Access management methods
  getEmployeeAccess(employeeId) {
    return fetch(`${BASE_URL}/access/by-employee/${employeeId}`).then(
      (response) => response.json()
    );
  },

  grantAccess(employeeId, areaId) {
    return fetch(
      `${BASE_URL}/access/grant?employeeId=${employeeId}&areaId=${areaId}`,
      {
        method: "POST",
      }
    ).then((response) => response.json());
  },

  grantAccessForAllArea(employeeId, areaId) {
    return fetch(
      `${BASE_URL}/access/grantAllAreas?employeeId=${employeeId}&areaId=${areaId}`,
      {
        method: "POST",
      }
    ).then((response) => response.json());
  },

  revokeAccess(accessId) {
    return fetch(`${BASE_URL}/access/revoke/${accessId}`, {
      method: "DELETE",
    }).then((response) => response.json());
  },

  // Fingerprint management methods
  getEmployeeFingerprints(employeeId, activeOnly = false) {
    return fetch(
      `${BASE_URL}/fingerprint-sample/employee/${employeeId}?activeOnly=${activeOnly}`
    ).then((response) => response.json());
  },

  setMaxFingerprintSamples(employeeId, maxSamples) {
    return fetch(
      `${BASE_URL}/fingerprint-sample/set-max-samples/${employeeId}?maxSamples=${maxSamples}`,
      {
        method: "PUT",
      }
    ).then((response) => response.json());
  },

  enableFingerprint(fingerprintId) {
    return fetch(`${BASE_URL}/fingerprint-sample/enable/${fingerprintId}`, {
      method: "PUT",
    }).then((response) => response.json());
  },

  disableFingerprint(fingerprintId) {
    return fetch(`${BASE_URL}/fingerprint-sample/disable/${fingerprintId}`, {
      method: "PUT",
    }).then((response) => response.json());
  },

  deleteFingerprint(fingerprintId) {
    return fetch(`${BASE_URL}/fingerprint-sample/${fingerprintId}`, {
      method: "DELETE",
    }).then((response) => response.json());
  },

  disableAllFingerprints(employeeId) {
    return fetch(`${BASE_URL}/fingerprint-sample/disable-all/${employeeId}`, {
      method: "PUT",
    }).then((response) => response.json());
  },

  enableAllFingerprints(employeeId) {
    return fetch(`${BASE_URL}/fingerprint-sample/enable-all/${employeeId}`, {
      method: "PUT",
    }).then((response) => response.json());
  },

  deleteAllFingerprints(employeeId) {
    return fetch(`${BASE_URL}/fingerprint-sample/delete-all/${employeeId}`, {
      method: "DELETE",
    }).then((response) => response.json());
  },

  // Model-related methods
  getSegmentationModels() {
    return fetch(`${BASE_URL}/fingerprint-segmentation-model`).then(
      (response) => response.json()
    );
  },

  getRecognitionModels() {
    return fetch(`${BASE_URL}/fingerprint-recognition-model`).then((response) =>
      response.json()
    );
  },

  getSegmentationModelStatistics() {
    return fetch(`${BASE_URL}/fingerprint-segmentation-model/statistics`).then(
      (response) => response.json()
    );
  },

  getRecognitionModelStatistics() {
    return fetch(`${BASE_URL}/fingerprint-recognition-model/statistics`).then(
      (response) => response.json()
    );
  },

  getModelDetails(type, modelId) {
    const endpoint =
      type === "segmentation"
        ? `/fingerprint-segmentation-model/${modelId}/statistics`
        : `/fingerprint-recognition-model/${modelId}/statistics`;
    return fetch(`${BASE_URL}${endpoint}`).then((response) => response.json());
  },

  // Fingerprint registration and recognition
  registerFingerprint(employeeId, formData) {
    return fetch(
      `${BASE_URL}/fingerprint-sample/register/${employeeId}/single`,
      {
        method: "POST",
        body: formData,
      }
    ).then((response) => response.json());
  },

  recognizeFingerprint(formData) {
    return fetch(`${BASE_URL}/fingerprint-recognition/recognize`, {
      method: "POST",
      body: formData,
    }).then((response) => response.json());
  },

  // Statistics and logs
  getEmployeeStatistics(startDate, endDate) {
    const url = new URL(`${BASE_URL}/employee/statistics`);
    url.searchParams.append("startDate", startDate);
    url.searchParams.append("endDate", endDate);
    return fetch(url).then((response) => response.json());
  },

  getEmployeeAccessLogs(employeeId, startDate, endDate, accessType = null, areaId = null) {
    const url = new URL(`${BASE_URL}/access-log/by-employee/${employeeId}`);
    url.searchParams.append("startDate", startDate);
    url.searchParams.append("endDate", endDate);
    
    if (accessType) {
      url.searchParams.append("accessType", accessType);
    }
    
    if (areaId) {
      url.searchParams.append("areaId", areaId);
    }
    
    return fetch(url).then((response) => response.json());
  },
  
  // Additional methods based on the provided JavaScript files
  getFingerSamplesForModel(modelId) {
    return fetch(
      `${BASE_URL}/fingerprint-sample/by-segmentation-model/${modelId}`
    ).then((response) => response.json());
  },

  getRecognitionsForModel(modelId) {
    return fetch(
      `${BASE_URL}/fingerprint-recognition/by-recognition-model/${modelId}`
    ).then((response) => response.json());
  },
  getSegmentationForModel(modelId) {
    return fetch(
      `${BASE_URL}/fingerprint-sample/by-segmentation-model/${modelId}`
    ).then((response) => response.json());
  }
};

export default apiService;
