const BASE_URL = "http://localhost:8080/api";

export const apiService = {
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

  getEmployees() {
    return fetch(`${BASE_URL}/employee`).then((response) => response.json());
  },

  getAreas() {
    return fetch(`${BASE_URL}/area`).then((response) => response.json());
  },

  getEmployeeAccess(employeeId) {
    return fetch(`${BASE_URL}/access/by-employee/${employeeId}`).then(
      (response) => response.json()
    );
  },

  grantAccess(body) {
    return fetch(`${BASE_URL}/access/grant`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    }).then((response) => {
      if (!response.ok) {
        throw new Error("Failed to grant access");
      }
    });
  },

  grantAccessForAllArea(employeeId) {
    return fetch(`${BASE_URL}/access/grant-all-areas/${employeeId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
    }).then((response) => {
      if (!response.ok) {
        throw new Error("Failed to grant access for all areas");
      }
    });
  },

  revokeAccess(accessId) {
    return fetch(`${BASE_URL}/access/revoke/${accessId}`, {
      method: "DELETE",
    }).then((response) => {
      if (!response.ok) {
        throw new Error("Failed to revoke access");
      }
      return;
    });
  },


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

  recognizeFingerprint(formData) {
    return fetch(`${BASE_URL}/fingerprint-recognition/recognize`, {
      method: "POST",
      body: formData,
    }).then((response) => response.json());
  },

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
};

export default apiService;
