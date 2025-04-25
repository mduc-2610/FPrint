// Global variables
const BASE_URL = 'http://localhost:8080/api';
let selectedEmployeeAccess = null;
let selectedEmployeeFingerprint = null;
let areaList = [];
let fingerprintList = [];
let toast;
let confirmationModal;

document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap components
    const tabElements = document.querySelectorAll('button[data-bs-toggle="tab"]');
    tabElements.forEach(tab => {
        tab.addEventListener('click', function() {
            const targetId = tab.getAttribute('data-bs-target').substring(1);
            document.querySelectorAll('.tab-pane').forEach(pane => {
                pane.classList.remove('show', 'active');
            });
            document.getElementById(targetId).classList.add('show', 'active');
        });
    });
    
    // Initialize toast
    toast = new bootstrap.Toast(document.getElementById('toast'));
    
    // Initialize confirmation modal
    confirmationModal = new bootstrap.Modal(document.getElementById('confirmationModal'));
    
    // Load employees and areas
    loadEmployeesForAccess();
    loadEmployeesForFingerprint();
    loadAreas();
    
    // Setup search functionality
    setupEmployeeSearch('employeeSearchAccess', 'employeeTableBodyAccess');
    setupEmployeeSearch('employeeSearchFingerprint', 'employeeTableBodyFingerprint');
    
    // Setup event listeners
    document.getElementById('grantAccessBtn').addEventListener('click', grantAccess);
    document.getElementById('setMaxSamplesBtn').addEventListener('click', setMaxFingerprintSamples);
    document.getElementById('disableAllFingerprintsBtn').addEventListener('click', confirmDisableAllFingerprints);
});

// ===== Employee Access Management =====

function loadEmployeesForAccess() {
    showLoading();
    
    fetch(`${BASE_URL}/employee`)
        .then(response => response.json())
        .then(employees => {
            populateEmployeeTable(employees, 'employeeTableBodyAccess', selectEmployeeForAccess);
            hideLoading();
        })
        .catch(error => {
            console.error('Error loading employees:', error);
            showToast('Error', 'Failed to load employees', 'error');
            hideLoading();
        });
}

function loadAreas() {
    fetch(`${BASE_URL}/area`)
        .then(response => response.json())
        .then(areas => {
            areaList = areas;
            populateAreaDropdown('areaSelectAccess', areas);
        })
        .catch(error => {
            console.error('Error loading areas:', error);
            showToast('Error', 'Failed to load areas', 'error');
        });
}

function populateAreaDropdown(elementId, areas) {
    const dropdown = document.getElementById(elementId);
    dropdown.innerHTML = '<option value="">-- Select Area --</option>';
    
    areas.forEach(area => {
        const option = document.createElement('option');
        option.value = area.id;
        option.textContent = area.name;
        dropdown.appendChild(option);
    });
}

function populateEmployeeTable(employees, tableBodyId, selectCallback) {
    const tableBody = document.getElementById(tableBodyId);
    tableBody.innerHTML = '';
    
    employees.forEach(employee => {
        const row = document.createElement('tr');
        row.setAttribute('data-employee-id', employee.id);
        row.addEventListener('click', () => selectCallback(employee));
        
        const photoCell = document.createElement('td');
        const photoImg = document.createElement('img');
        photoImg.src = employee.photoUrl || 'https://th.bing.com/th/id/OIP.hlxam_68Up5ge-CcOHvhLQHaHa?w=512&h=512&rs=1&pid=ImgDetMain';
        photoImg.alt = 'Employee Photo';
        photoImg.style.width = '40px';
        photoImg.style.height = '40px';
        photoImg.className = 'rounded-circle';
        photoCell.appendChild(photoImg);
        
        const idCell = document.createElement('td');
        idCell.textContent = employee.id;
        
        const nameCell = document.createElement('td');
        nameCell.textContent = employee.fullName;
        
        row.appendChild(photoCell);
        row.appendChild(idCell);
        row.appendChild(nameCell);
        
        // Add fingerprint count cell for fingerprint management tab
        if (tableBodyId === 'employeeTableBodyFingerprint') {
            const fingerprintCell = document.createElement('td');
            
            // Fetch fingerprint count
            fetch(`${BASE_URL}/fingerprint-sample/employee/${employee.id}`)
                .then(response => response.json())
                .then(samples => {
                    const activeSamples = samples.filter(sample => sample.active).length;
                    fingerprintCell.textContent = `${activeSamples} / ${employee.maxNumberSamples}`;
                })
                .catch(error => {
                    console.error('Error loading fingerprint count:', error);
                    fingerprintCell.textContent = 'Error';
                });
            
            row.appendChild(fingerprintCell);
        }
        
        tableBody.appendChild(row);
    });
}

function setupEmployeeSearch(inputId, tableBodyId) {
    const searchInput = document.getElementById(inputId);
    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        const rows = document.querySelectorAll(`#${tableBodyId} tr`);
        
        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            if (text.includes(searchTerm)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    });
}

function selectEmployeeForAccess(employee) {
    selectedEmployeeAccess = employee;
    
    // Update UI to show selected employee
    document.getElementById('employeeAccessInfo').classList.remove('d-none');
    document.getElementById('areaAccessManagement').classList.remove('d-none');
    document.getElementById('employeeAccessPlaceholder').classList.add('d-none');
    
    document.getElementById('selectedEmployeePhotoAccess').src = employee.photoUrl || 'https://th.bing.com/th/id/OIP.hlxam_68Up5ge-CcOHvhLQHaHa?w=512&h=512&rs=1&pid=ImgDetMain';
    document.getElementById('selectedEmployeeNameAccess').textContent = employee.fullName;
    document.getElementById('selectedEmployeeIdAccess').textContent = employee.id;
    
    // Highlight the selected row
    const rows = document.querySelectorAll('#employeeTableBodyAccess tr');
    rows.forEach(row => row.classList.remove('selected-row'));
    
    const selectedRow = document.querySelector(`#employeeTableBodyAccess tr[data-employee-id="${employee.id}"]`);
    if (selectedRow) {
        selectedRow.classList.add('selected-row');
    }
    
    // Load employee's access permissions
    loadEmployeeAccess(employee.id);
}

function loadEmployeeAccess(employeeId) {
    showLoading();
    
    fetch(`${BASE_URL}/access/by-employee/${employeeId}`)
        .then(response => response.json())
        .then(accessList => {
            populateAccessTable(accessList);
            hideLoading();
        })
        .catch(error => {
            console.error('Error loading employee access:', error);
            showToast('Error', 'Failed to load access permissions', 'error');
            hideLoading();
        });
}

function populateAccessTable(accessList) {
    const tableBody = document.getElementById('employeeAccessTableBody');
    tableBody.innerHTML = '';
    
    if (accessList.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 3;
        cell.textContent = 'No access permissions found';
        cell.className = 'text-center';
        row.appendChild(cell);
        tableBody.appendChild(row);
        return;
    }
    
    accessList.forEach(access => {
        const row = document.createElement('tr');
        
        const areaCell = document.createElement('td');
        areaCell.textContent = access.area.name;
        
        const timestampCell = document.createElement('td');
        timestampCell.textContent = new Date(access.timestamp).toLocaleString();
        
        const actionsCell = document.createElement('td');
        const revokeBtn = document.createElement('button');
        revokeBtn.className = 'btn btn-sm btn-danger';
        revokeBtn.innerHTML = '<i class="fas fa-trash"></i>';
        revokeBtn.title = 'Revoke Access';
        revokeBtn.addEventListener('click', () => confirmRevokeAccess(access.id));
        actionsCell.appendChild(revokeBtn);
        
        row.appendChild(areaCell);
        row.appendChild(timestampCell);
        row.appendChild(actionsCell);
        
        tableBody.appendChild(row);
    });
}

function grantAccess() {
    const areaId = document.getElementById('areaSelectAccess').value;
    
    if (!selectedEmployeeAccess) {
        showToast('Error', 'Please select an employee first', 'error');
        return;
    }
    
    if (!areaId) {
        showToast('Error', 'Please select an area', 'error');
        return;
    }
    
    showLoading();
    
    fetch(`${BASE_URL}/access/grant?employeeId=${selectedEmployeeAccess.id}&areaId=${areaId}`, {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Failed to grant access');
                });
            }
            return response.json();
        })
        .then(data => {
            showToast('Success', 'Access granted successfully', 'success');
            loadEmployeeAccess(selectedEmployeeAccess.id);
        })
        .catch(error => {
            console.error('Error granting access:', error);
            showToast('Error', error.message || 'Failed to grant access', 'error');
            hideLoading();
        });
}

function confirmRevokeAccess(accessId) {
    document.getElementById('confirmationModalTitle').textContent = 'Revoke Access';
    document.getElementById('confirmationModalBody').textContent = 'Are you sure you want to revoke this access permission?';
    
    const confirmBtn = document.getElementById('confirmationModalConfirm');
    confirmBtn.onclick = () => {
        confirmationModal.hide();
        revokeAccess(accessId);
    };
    
    confirmationModal.show();
}

function revokeAccess(accessId) {
    showLoading();
    
    fetch(`${BASE_URL}/access/revoke/${accessId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Failed to revoke access');
                });
            }
            return response.json();
        })
        .then(data => {
            showToast('Success', 'Access revoked successfully', 'success');
            loadEmployeeAccess(selectedEmployeeAccess.id);
        })
        .catch(error => {
            console.error('Error revoking access:', error);
            showToast('Error', error.message || 'Failed to revoke access', 'error');
            hideLoading();
        });
}

// ===== Fingerprint Management =====

function loadEmployeesForFingerprint() {
    showLoading();
    
    fetch(`${BASE_URL}/employee`)
        .then(response => response.json())
        .then(employees => {
            populateEmployeeTable(employees, 'employeeTableBodyFingerprint', selectEmployeeForFingerprint);
            hideLoading();
        })
        .catch(error => {
            console.error('Error loading employees:', error);
            showToast('Error', 'Failed to load employees', 'error');
            hideLoading();
        });
}

function selectEmployeeForFingerprint(employee) {
    selectedEmployeeFingerprint = employee;
    
    // Update UI to show selected employee
    document.getElementById('employeeInfoFingerprint').classList.remove('d-none');
    document.getElementById('fingerprintPlaceholder').classList.add('d-none');
    document.getElementById('fingerprintContainer').classList.remove('d-none');
    document.getElementById('fingerprintActionsContainer').classList.remove('d-none');
    
    document.getElementById('selectedEmployeePhotoFingerprint').src = employee.photoUrl || 'https://th.bing.com/th/id/OIP.hlxam_68Up5ge-CcOHvhLQHaHa?w=512&h=512&rs=1&pid=ImgDetMain';
    document.getElementById('selectedEmployeeNameFingerprint').textContent = employee.fullName;
    document.getElementById('selectedEmployeeIdFingerprint').textContent = employee.id;
    document.getElementById('maxFingerprintCount').textContent = employee.maxNumberSamples || 'N/A';
    document.getElementById('maxFingerprintSamples').value = employee.maxNumberSamples;
    
    // Highlight the selected row
    const rows = document.querySelectorAll('#employeeTableBodyFingerprint tr');
    rows.forEach(row => row.classList.remove('selected-row'));
    
    const selectedRow = document.querySelector(`#employeeTableBodyFingerprint tr[data-employee-id="${employee.id}"]`);
    if (selectedRow) {
        selectedRow.classList.add('selected-row');
    }
    
    // Load employee's fingerprint samples
    loadEmployeeFingerprints(employee.id);
}

function loadEmployeeFingerprints(employeeId) {
    showLoading();
    
    fetch(`${BASE_URL}/fingerprint-sample/employee/${employeeId}?activeOnly=false`)
        .then(response => response.json())
        .then(samples => {
            fingerprintList = samples;
            displayFingerprints(samples);
            
            // Update active count
            const activeCount = samples.filter(sample => sample.active).length;
            document.getElementById('activeFingerprintCount').textContent = activeCount;
            
            hideLoading();
        })
        .catch(error => {
            console.error('Error loading fingerprint samples:', error);
            showToast('Error', 'Failed to load fingerprint samples', 'error');
            hideLoading();
        });
}

function displayFingerprints(samples) {
    const container = document.getElementById('fingerprintContainer');
    container.innerHTML = '';
    
    const noFingerprintsMessage = document.getElementById('noFingerprintsMessage');
    
    if (samples.length === 0) {
        noFingerprintsMessage.classList.remove('d-none');
        return;
    } else {
        noFingerprintsMessage.classList.add('d-none');
    }
    
    samples.forEach(sample => {
        const card = document.createElement('div');
        card.className = `card fingerprint-sample-card ${sample.active ? '' : 'inactive'}`;
        
        const cardHeader = document.createElement('div');
        cardHeader.className = 'card-header d-flex justify-content-between align-items-center';
        
        const position = document.createElement('h6');
        position.className = 'mb-0';
        position.textContent = formatPosition(sample.position);
        
        const statusBadge = document.createElement('span');
        statusBadge.className = `badge ${sample.active ? 'bg-success' : 'bg-secondary'}`;
        statusBadge.textContent = sample.active ? 'Active' : 'Inactive';
        
        cardHeader.appendChild(position);
        cardHeader.appendChild(statusBadge);
        
        const cardBody = document.createElement('div');
        cardBody.className = 'card-body text-center';
        
        const fingerprintIcon = document.createElement('i');
        fingerprintIcon.className = 'fas fa-fingerprint fa-4x mb-3';
        fingerprintIcon.style.color = sample.active ? '#007bff' : '#6c757d';
        
        const sampleInfo = document.createElement('div');
        sampleInfo.innerHTML = `
            <p class="mb-1"><small>ID: ${sample.id.substring(0, 8)}...</small></p>
            <p class="mb-1"><small>Captured: ${new Date(sample.capturedAt).toLocaleDateString()}</small></p>
            ${sample.quality ? `<p class="mb-0"><small>Quality: ${(sample.quality * 100).toFixed(2)}%</small></p>` : ''}
        `;
        
        const cardFooter = document.createElement('div');
        cardFooter.className = 'card-footer';
        
        const buttonGroup = document.createElement('div');
        buttonGroup.className = 'fingerprint-sample-actions';
        
        if (sample.active) {
            const disableBtn = document.createElement('button');
            disableBtn.className = 'btn btn-sm btn-warning';
            disableBtn.innerHTML = '<i class="fas fa-ban"></i>';
            disableBtn.title = 'Disable Fingerprint';
            disableBtn.addEventListener('click', () => disableFingerprint(sample.id));
            buttonGroup.appendChild(disableBtn);
        } else {
            const enableBtn = document.createElement('button');
            enableBtn.className = 'btn btn-sm btn-success';
            enableBtn.innerHTML = '<i class="fas fa-check"></i>';
            enableBtn.title = 'Enable Fingerprint';
            enableBtn.addEventListener('click', () => enableFingerprint(sample.id));
            buttonGroup.appendChild(enableBtn);
        }
        
        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'btn btn-sm btn-danger';
        deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
        deleteBtn.title = 'Delete Fingerprint';
        deleteBtn.addEventListener('click', () => confirmDeleteFingerprint(sample.id));
        buttonGroup.appendChild(deleteBtn);
        
        cardFooter.appendChild(buttonGroup);
        
        cardBody.appendChild(fingerprintIcon);
        cardBody.appendChild(sampleInfo);
        
        card.appendChild(cardHeader);
        card.appendChild(cardBody);
        card.appendChild(cardFooter);
        
        container.appendChild(card);
    });
}

function formatPosition(position) {
    if (!position) return 'Unknown';
    return position.replace('_', ' ').toLowerCase()
        .replace(/\b\w/g, letter => letter.toUpperCase());
}

function disableFingerprint(fingerprintId) {
    showLoading();
    
    fetch(`${BASE_URL}/fingerprint-sample/disable/${fingerprintId}`, {
        method: 'PUT'
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Failed to disable fingerprint');
                });
            }
            return response.json();
        })
        .then(data => {
            showToast('Success', 'Fingerprint disabled successfully', 'success');
            loadEmployeeFingerprints(selectedEmployeeFingerprint.id);
            updateFingerprintCount(selectedEmployeeFingerprint.id);
        })
        .catch(error => {
            console.error('Error disabling fingerprint:', error);
            showToast('Error', error.message || 'Failed to disable fingerprint', 'error');
            hideLoading();
        });
}

function enableFingerprint(fingerprintId) {
    showLoading();
    
    fetch(`${BASE_URL}/fingerprint-sample/enable/${fingerprintId}`, {
        method: 'PUT'
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Failed to enable fingerprint');
                });
            }
            return response.json();
        })
        .then(data => {
            showToast('Success', 'Fingerprint enabled successfully', 'success');
            loadEmployeeFingerprints(selectedEmployeeFingerprint.id);
            updateFingerprintCount(selectedEmployeeFingerprint.id);
        })
        .catch(error => {
            console.error('Error enabling fingerprint:', error);
            showToast('Error', error.message || 'Failed to enable fingerprint', 'error');
            hideLoading();
        });
}

function confirmDeleteFingerprint(fingerprintId) {
    document.getElementById('confirmationModalTitle').textContent = 'Delete Fingerprint';
    document.getElementById('confirmationModalBody').textContent = 'Are you sure you want to delete this fingerprint sample? This action cannot be undone.';
    
    const confirmBtn = document.getElementById('confirmationModalConfirm');
    confirmBtn.onclick = () => {
        confirmationModal.hide();
        deleteFingerprint(fingerprintId);
    };
    
    confirmationModal.show();
}

function deleteFingerprint(fingerprintId) {
    showLoading();
    
    fetch(`${BASE_URL}/fingerprint-sample/${fingerprintId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Failed to delete fingerprint');
                });
            }
            return response.json();
        })
        .then(data => {
            showToast('Success', 'Fingerprint deleted successfully', 'success');
            loadEmployeeFingerprints(selectedEmployeeFingerprint.id);
            updateFingerprintCount(selectedEmployeeFingerprint.id);
        })
        .catch(error => {
            console.error('Error deleting fingerprint:', error);
            showToast('Error', error.message || 'Failed to delete fingerprint', 'error');
            hideLoading();
        });
}

function confirmDisableAllFingerprints() {
    document.getElementById('confirmationModalTitle').textContent = 'Disable All Fingerprints';
    document.getElementById('confirmationModalBody').textContent = 'Are you sure you want to disable all fingerprints for this employee?';
    
    const confirmBtn = document.getElementById('confirmationModalConfirm');
    confirmBtn.onclick = () => {
        confirmationModal.hide();
        disableAllFingerprints();
    };
    
    confirmationModal.show();
}

function disableAllFingerprints() {
    showLoading();
    
    fetch(`${BASE_URL}/fingerprint-sample/disable-all/${selectedEmployeeFingerprint.id}`, {
        method: 'PUT'
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Failed to disable fingerprints');
                });
            }
            return response.json();
        })
        .then(data => {
            showToast('Success', `All fingerprints disabled successfully (${data.disabledCount} samples)`, 'success');
            loadEmployeeFingerprints(selectedEmployeeFingerprint.id);
            updateFingerprintCount(selectedEmployeeFingerprint.id);
        })
        .catch(error => {
            console.error('Error disabling all fingerprints:', error);
            showToast('Error', error.message || 'Failed to disable fingerprints', 'error');
            hideLoading();
        });
}

function setMaxFingerprintSamples() {
    const maxSamples = document.getElementById('maxFingerprintSamples').value;
    
    if (!maxSamples || maxSamples < 1) {
        showToast('Error', 'Please enter a valid number (minimum 1)', 'error');
        return;
    }
    
    showLoading();
    
    fetch(`${BASE_URL}/fingerprint-sample/set-max-samples/${selectedEmployeeFingerprint.id}?maxSamples=${maxSamples}`, {
        method: 'PUT'
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Failed to set max samples');
                });
            }
            return response.json();
        })
        .then(data => {
            showToast('Success', `Maximum fingerprint samples updated to ${maxSamples}`, 'success');
            document.getElementById('maxFingerprintCount').textContent = maxSamples;
            updateFingerprintCount(selectedEmployeeFingerprint.id);
            hideLoading();
        })
        .catch(error => {
            console.error('Error setting max samples:', error);
            showToast('Error', error.message || 'Failed to set max samples', 'error');
            hideLoading();
        });
}

function updateFingerprintCount(employeeId) {
    // Update fingerprint count in the employee table
    fetch(`${BASE_URL}/fingerprint-sample/employee/${employeeId}`)
        .then(response => response.json())
        .then(samples => {
            const activeSamples = samples.filter(sample => sample.active).length;
            
            // Update in fingerprint management info
            document.getElementById('activeFingerprintCount').textContent = activeSamples;
            
            // Update in employee table
            const row = document.querySelector(`#employeeTableBodyFingerprint tr[data-employee-id="${employeeId}"]`);
            if (row) {
                const fingerprintCell = row.querySelector('td:last-child');
                if (fingerprintCell) {
                    fingerprintCell.textContent = `${activeSamples} / ${selectedEmployeeFingerprint.maxNumberSamples || 'N/A'}`;
                }
            }
        })
        .catch(error => {
            console.error('Error updating fingerprint count:', error);
        });
}

// ===== Utility Functions =====

function showLoading() {
    document.getElementById('loadingIndicator').style.display = 'flex';
}

function hideLoading() {
    document.getElementById('loadingIndicator').style.display = 'none';
}

function showToast(title, message, type = 'info') {
    const toastElement = document.getElementById('toast');
    const toastTitle = document.getElementById('toastTitle');
    const toastMessage = document.getElementById('toastMessage');
    
    toastTitle.textContent = title;
    toastMessage.textContent = message;
    
    // Set toast color based on type
    toastElement.className = 'toast';
    if (type === 'success') {
        toastElement.classList.add('bg-success', 'text-white');
    } else if (type === 'error') {
        toastElement.classList.add('bg-danger', 'text-white');
    } else if (type === 'warning') {
        toastElement.classList.add('bg-warning');
    } else {
        toastElement.classList.add('bg-info', 'text-white');
    }
    
    toast.show();
}