// Global variables
let selectedEmployee = null;
let selectedArea = null;
let selectedStatisticsEmployee = null;
const BASE_URL = 'http://localhost:8080/api';
const BASE_ACCESS_CONTROL_SERVICE = `${BASE_URL}/api/access`;
const BASE_USER_SERVICE = `${BASE_URL}/api/users`;
const BASE_BIOMETRICS_SERVICE = `${BASE_URL}/api/biometrics`;
const BASE_MODEL_SERVICE = `${BASE_URL}/api/models`;
const BASE_TRAINING_SERVICE = `${BASE_URL}/api/training`;
const SEGMENTATION_MODEL_STATISTICS_URL = `${BASE_MODEL_SERVICE}/fingerprint-segmentation-model/statistics`;
const RECOGNITION_MODEL_STATISTICS_URL = `${BASE_MODEL_SERVICE}/fingerprint-recognition-model/statistics`;

let segmentationModelTableBody;
let recognitionModelTableBody;
let selectedModelDetails = null;

// Function to initialize models tab
function initModelsTab() {
    segmentationModelTableBody = document.getElementById('segmentationModelTableBody');
    recognitionModelTableBody = document.getElementById('recognitionModelTableBody');
    
    // Load initial model data
    loadSegmentationModels();
    loadRecognitionModels();
    
    // Add event listeners for model rows
    segmentationModelTableBody.addEventListener('click', handleSegmentationModelClick);
    recognitionModelTableBody.addEventListener('click', handleRecognitionModelClick);
}

// Function to load segmentation models with statistics
async function loadSegmentationModels() {
    try {
        showLoading();
        const response = await fetch(`${BASE_URL}/fingerprint-segmentation-model/statistics`);
        
        if (!response.ok) {
            throw new Error(`Failed to load segmentation models: ${response.status}`);
        }
        
        const models = await response.json();
        renderSegmentationModels(models);
    } catch (error) {
        console.error('Error loading segmentation models:', error);
        showAlert('danger', `Failed to load segmentation models: ${error.message}`);
    } finally {
        hideLoading();
    }
}

// Function to load recognition models with statistics
async function loadRecognitionModels() {
    try {
        showLoading();
        const response = await fetch(`${BASE_URL}/fingerprint-recognition-model/statistics`);
        
        if (!response.ok) {
            throw new Error(`Failed to load recognition models: ${response.status}`);
        }
        
        const models = await response.json();
        renderRecognitionModels(models);
    } catch (error) {
        console.error('Error loading recognition models:', error);
        showAlert('danger', `Failed to load recognition models: ${error.message}`);
    } finally {
        hideLoading();
    }
}

// Render segmentation models table
function renderSegmentationModels(models) {
    segmentationModelTableBody.innerHTML = '';
    
    if (models.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="3" class="text-center">No segmentation models found</td>';
        segmentationModelTableBody.appendChild(row);
        return;
    }
    
    models.forEach(model => {
        const row = document.createElement('tr');
        row.setAttribute('data-model-id', model.model.id);
        row.className = 'model-row';
        
        const createdDate = model.totalUsage != null ? model.totalUsage : 'N/A';
        
        row.innerHTML = `
            <td>${model.model.name}</td>
            <td>${model.averageConfidence ? model.averageConfidence.toFixed(2) : 'N/A'}</td>
            <td>${createdDate}</td>
        `;
        
        segmentationModelTableBody.appendChild(row);
    });
}

// Render recognition models table
function renderRecognitionModels(models) {
    recognitionModelTableBody.innerHTML = '';
    
    if (models.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="3" class="text-center">No recognition models found</td>';
        recognitionModelTableBody.appendChild(row);
        return;
    }
    
    models.forEach(model => {
        const row = document.createElement('tr');
        row.setAttribute('data-model-id', model.model.id);
        row.className = 'model-row';
        
        const createdDate = model.totalUsage != null ? model.totalUsage : 'N/A';
        
        row.innerHTML = `
            <td>${model.model.name}</td>
            <td>${model.averageConfidence ? model.averageConfidence.toFixed(2) : 'N/A'}</td>
            <td>${createdDate}</td>
        `;
        
        recognitionModelTableBody.appendChild(row);
    });
}

// Handle clicking on a segmentation model
async function handleSegmentationModelClick(event) {
    const row = event.target.closest('tr');
    if (!row || !row.classList.contains('model-row')) return;
    
    const modelId = row.getAttribute('data-model-id');
    if (!modelId) return;
    
    try {
        showLoading();
        
        // Get the model details first
        const modelResponse = await fetch(`${BASE_URL}/fingerprint-segmentation-model/${modelId}/statistics`);
        if (!modelResponse.ok) {
            throw new Error(`Failed to load model details: ${modelResponse.status}`);
        }
        const modelDetails = await modelResponse.json();
        
        // Get the fingerprint samples that used this model
        const samplesResponse = await fetch(`${BASE_URL}/fingerprint-sample/by-segmentation-model/${modelId}`);
        if (!samplesResponse.ok) {
            throw new Error(`Failed to load fingerprint samples: ${samplesResponse.status}`);
        }
        const samples = await samplesResponse.json();
        
        // Create and show modal with model details and samples
        showModelDetailsModal('Segmentation Model Details', modelDetails, samples, 'fingerprint');
    } catch (error) {
        console.error('Error loading segmentation model details:', error);
        showAlert('danger', `Failed to load model details: ${error.message}`);
    } finally {
        hideLoading();
    }
}

// Handle clicking on a recognition model
async function handleRecognitionModelClick(event) {
    const row = event.target.closest('tr');
    if (!row || !row.classList.contains('model-row')) return;
    
    const modelId = row.getAttribute('data-model-id');
    if (!modelId) return;
    
    try {
        showLoading();
        
        // Get the model details first
        const modelResponse = await fetch(`${BASE_URL}/fingerprint-recognition-model/${modelId}/statistics`);
        if (!modelResponse.ok) {
            throw new Error(`Failed to load model details: ${modelResponse.status}`);
        }
        const modelDetails = await modelResponse.json();
        
        // Get the recognitions that used this model
        const recognitionsResponse = await fetch(`${BASE_URL}/fingerprint-recognition/by-recognition-model/${modelId}`);
        if (!recognitionsResponse.ok) {
            throw new Error(`Failed to load recognitions: ${recognitionsResponse.status}`);
        }
        const recognitions = await recognitionsResponse.json();
        
        // Create and show modal with model details and recognitions
        showModelDetailsModal('Recognition Model Details', modelDetails, recognitions, 'recognition');
    } catch (error) {
        console.error('Error loading recognition model details:', error);
        showAlert('danger', `Failed to load model details: ${error.message}`);
    } finally {
        hideLoading();
    }
}

// Function to show model details modal
function showModelDetailsModal(title, modelDetails, items, type) {
    // Check if modal already exists, remove it if it does
    let existingModal = document.getElementById('modelDetailsModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // Create modal structure
    const modal = document.createElement('div');
    modal.id = 'modelDetailsModal';
    modal.className = 'modal fade';
    modal.tabIndex = '-1';
    modal.setAttribute('aria-labelledby', 'modelDetailsModalLabel');
    modal.setAttribute('aria-hidden', 'true');
    
    // Create modal content based on type (fingerprint or recognition)
    let tableContent = '';
    
    if (type === 'fingerprint') {
        tableContent = `
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Employee ID</th>
                        <th>Position</th>
                        <th>Quality</th>
                        <th>Captured At</th>
                    </tr>
                </thead>
                <tbody>
                    ${items.length > 0 ? 
                        items.map(item => `
                            <tr>
                                <td>${item.employeeId || 'N/A'}</td>
                                <td>${item.position || 'N/A'}</td>
                                <td>${item.quality ? item.quality.toFixed(2) : 'N/A'}</td>
                                <td>${item.capturedAt ? new Date(item.capturedAt).toLocaleString() : 'N/A'}</td>
                            </tr>
                        `).join('') : 
                        '<tr><td colspan="4" class="text-center">No fingerprint samples found</td></tr>'
                    }
                </tbody>
            </table>
        `;
    } else { // recognition
        tableContent = `
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Employee ID</th>
                        <th>Timestamp</th>
                        <th>Confidence</th>
                        <th>Access Log ID</th>
                    </tr>
                </thead>
                <tbody>
                    ${items.length > 0 ? 
                        items.map(item => `
                            <tr>
                                <td>${item.employeeId || 'N/A'}</td>
                                <td>${item.timestamp ? new Date(item.timestamp).toLocaleString() : 'N/A'}</td>
                                <td>${item.confidence ? item.confidence.toFixed(2) : 'N/A'}</td>
                                <td>${item.accessLogId || 'N/A'}</td>
                            </tr>
                        `).join('') : 
                        '<tr><td colspan="4" class="text-center">No recognitions found</td></tr>'
                    }
                </tbody>
            </table>
        `;
    }
    
    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="modelDetailsModalLabel">${title}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="card mb-3">
                        <div class="card-header">
                            <h6>Model Information</h6>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong>Model ID:</strong> ${modelDetails.model.id}</p>
                                    <p><strong>Model Name:</strong> ${modelDetails.model.name}</p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong>Total Usage:</strong> ${modelDetails.totalUsage}</p>
                                    ${type == "recognition" ? `<p><strong>Average Confidence:</strong> ${modelDetails.averageConfidence ? modelDetails.averageConfidence.toFixed(2) : 'N/A'}</p>` : ""}
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="card">
                        <div class="card-header">
                            <h6>${type === 'fingerprint' ? 'Fingerprint Samples' : 'Recognition History'}</h6>
                        </div>
                        <div class="card-body">
                            <div class="table-container" style="max-height: 300px; overflow-y: auto;">
                                ${tableContent}
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Initialize and show the modal
    const modalInstance = new bootstrap.Modal(modal);
    modalInstance.show();
}

// Helper function to show loading indicator
function showLoading() {
    const loadingIndicator = document.getElementById('loadingIndicator');
    if (loadingIndicator) {
        loadingIndicator.style.display = 'flex';
    }
}

// Helper function to hide loading indicator
function hideLoading() {
    const loadingIndicator = document.getElementById('loadingIndicator');
    if (loadingIndicator) {
        loadingIndicator.style.display = 'none';
    }
}

// Helper function to show alert messages
function showAlert(type, message, containerId = 'recognizeResultContent') {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    container.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
}

function loadAreasForFilter() {
    fetch(`${BASE_URL}/area`)
        .then(response => response.json())
        .then(data => {
            const areaFilter = document.getElementById('areaFilter');
            areaFilter.innerHTML = '<option value="">All Areas</option>';
            
            data.forEach(area => {
                const option = document.createElement('option');
                option.value = area.id;
                option.textContent = area.name;
                areaFilter.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error loading areas:', error);
        });
}

function setupEmployeeStatistics() {
    const loadStatisticsBtn = document.getElementById('loadStatisticsBtn');
    const startDateInput = document.getElementById('statisticsStartDate');
    const endDateInput = document.getElementById('statisticsEndDate');
    const accessTypeFilter = document.getElementById('accessTypeFilter');
    const areaFilter = document.getElementById('areaFilter');

    // Set default date range to last 30 days
    const defaultEndDate = new Date();
    const defaultStartDate = new Date();
    defaultStartDate.setDate(defaultStartDate.getDate() - 30);
    
    // Format dates to datetime-local input format
    startDateInput.value = formatDateForInput(defaultStartDate);
    endDateInput.value = formatDateForInput(defaultEndDate);

    loadStatisticsBtn.addEventListener('click', loadEmployeeStatistics);

    // Add event listeners for access logs filtering
    accessTypeFilter.addEventListener('change', filterEmployeeAccessLogs);
    areaFilter.addEventListener('change', filterEmployeeAccessLogs);
}

// Helper function to format date for datetime-local input
function formatDateForInput(date) {
    const pad = (num) => num.toString().padStart(2, '0');
    
    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());
    
    return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function loadEmployeeStatistics() {
    const startDateInput = document.getElementById('statisticsStartDate');
    const endDateInput = document.getElementById('statisticsEndDate');
    const tableBody = document.getElementById('employeeStatisticsTableBody');

    const startDate = startDateInput.value;
    const endDate = endDateInput.value;

    showLoading();

    fetch(`${BASE_URL}/employee/statistics?startDate=${startDate}&endDate=${endDate}`)
        .then(response => response.json())
        .then(data => {
            tableBody.innerHTML = '';
            
            data.forEach(stat => {
                const row = document.createElement('tr');
                row.setAttribute('data-employee-id', stat.employeeId);
                row.addEventListener('click', () => loadEmployeeAccessLogs(stat.employeeId));

                const photoCell = document.createElement('td');
                const photoImg = document.createElement('img');
                photoImg.src = stat.photoUrl || 'https://th.bing.com/th/id/OIP.hlxam_68Up5ge-CcOHvhLQHaHa?w=512&h=512&rs=1&pid=ImgDetMain';
                photoImg.alt = 'Employee Photo';
                photoImg.style.width = '50px';
                photoImg.style.height = '50px';
                photoImg.className = 'rounded-circle';
                photoCell.appendChild(photoImg);

                const idCell = document.createElement('td');
                idCell.textContent = stat.employeeId;

                const nameCell = document.createElement('td');
                nameCell.textContent = stat.fullName;

                const totalAccessCell = document.createElement('td');
                totalAccessCell.textContent = stat.totalAccesses;

                const authorizedAccessCell = document.createElement('td');
                authorizedAccessCell.textContent = stat.authorizedAccess;

                const unauthorizedAccessCell = document.createElement('td');
                unauthorizedAccessCell.textContent = stat.unauthorizedAccess;

                row.appendChild(photoCell);
                row.appendChild(idCell);
                row.appendChild(nameCell);
                row.appendChild(totalAccessCell);
                row.appendChild(authorizedAccessCell);
                row.appendChild(unauthorizedAccessCell);

                tableBody.appendChild(row);
            });

            hideLoading();
        })
        .catch(error => {
            console.error('Error loading employee statistics:', error);
            hideLoading();
        });
}

function loadEmployeeAccessLogs(employeeId) {
    const startDateInput = document.getElementById('statisticsStartDate');
    const endDateInput = document.getElementById('statisticsEndDate');
    const accessLogsCard = document.getElementById('employeeAccessLogsCard');
    const tableBody = document.getElementById('employeeAccessLogsTableBody');
    const accessTypeFilter = document.getElementById('accessTypeFilter');
    const areaFilter = document.getElementById('areaFilter');

    // Reset filters
    accessTypeFilter.value = '';
    areaFilter.value = '';

    const startDate = startDateInput.value;
    const endDate = endDateInput.value;

    showLoading();

    fetch(`${BASE_URL}/access-log/by-employee/${employeeId}?startDate=${startDate}&endDate=${endDate}`)
        .then(response => response.json())
        .then(data => {
            tableBody.innerHTML = '';
            
            data.forEach(log => {
                const row = document.createElement('tr');
                row.classList.add(log.authorized ? 'table-success' : 'table-danger');

                const timestampCell = document.createElement('td');
                timestampCell.textContent = new Date(log.timestamp).toLocaleString();

                const accessTypeCell = document.createElement('td');
                accessTypeCell.textContent = log.accessType;

                const areaCell = document.createElement('td');
                areaCell.textContent = log.area?.name || 'General Access';

                
                row.appendChild(timestampCell);
                row.appendChild(accessTypeCell);
                row.appendChild(areaCell);
                // const authorizationCell = document.createElement('td');
                // authorizationCell.textContent = log.authorized ? 'Authorized' : 'Unauthorized';
                // row.appendChild(authorizationCell);

                tableBody.appendChild(row);
            });

            accessLogsCard.style.display = 'block';
            hideLoading();
        })
        .catch(error => {
            console.error('Error loading employee access logs:', error);
            hideLoading();
        });
}

function filterEmployeeAccessLogs() {
    const tableBody = document.getElementById('employeeAccessLogsTableBody');
    const accessTypeFilterElement = document.getElementById('accessTypeFilter');
    const areaFilterElement = document.getElementById('areaFilter');
    const areaFilter = areaFilterElement.options[areaFilterElement.selectedIndex].textContent.trim().toLowerCase();
    const accessTypeFilter = accessTypeFilterElement.options[accessTypeFilterElement.selectedIndex].textContent.trim().toLowerCase();
    
    const rows = tableBody.querySelectorAll('tr');
    rows.forEach((row, index) => {
        const accessType = row.querySelector('td:nth-child(2)').textContent.trim().toLowerCase();
        const area = row.querySelector('td:nth-child(3)').textContent.trim().toLowerCase();
        // console.log("", accessType, accessTypeFilter, index);
        // console.log("Row content: ", area, " - Filter: ", areaFilter, index);    
    
        const accessTypeMatch = accessTypeFilter === 'all types' || !accessTypeFilter || accessType === accessTypeFilter;
        const areaMatch = areaFilter === 'all areas' || !areaFilter || area === areaFilter;

        row.style.display = (accessTypeMatch && areaMatch) ? '' : 'none';
    });
}
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap tabs
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

    // Load models on page load
    loadModels();
    
    // Load employee data
    loadEmployees();
    
    // Load area data
    loadAreas();
    
    loadAreasForFilter();
    // Setup search functionality
    
    // Setup form submissions
    setupRegisterForm();
    setupRecognizeForm();
    setupEmployeeSearch();
    
    // Setup file preview

    setupEmployeeStatistics();

    setupFilePreview();

    initModelsTab();
    
    // Add event listener for the models tab button to refresh data when clicked
    const modelsTab = document.getElementById('models-tab');
    if (modelsTab) {
        modelsTab.addEventListener('click', function() {
            loadSegmentationModels();
            loadRecognitionModels();
        });
    }
});

// Models functions

function loadModels() {
    showLoading();
    
    fetch(`${BASE_URL}/fingerprint-segmentation-model`)
        .then(response => response.json())
        .then(data => {
            populateModelDropdown('segmentationModel', data);
            populateModelDropdown('segmentationModelRecognize', data);
            // populateModelTable('segmentationModelTableBody', data);
        })
        .catch(error => {
            console.error('Error loading segmentation models:', error);
        });
    
        fetch(`${BASE_URL}/fingerprint-recognition-model`)
        .then(response => response.json())
        .then(data => {
            populateModelDropdown('recognitionModel', data);
            populateModelDropdown('recognitionModelRegister', data);
            // populateModelTable('recognitionModelTableBody', data);
            hideLoading();
        })
        .catch(error => {
            console.error('Error loading recognition models:', error);
            hideLoading();
        });
}

// Area functions
function loadAreas() {
    fetch(`${BASE_URL}/area`)
        .then(response => response.json())
        .then(data => {
            populateAreaDropdown('areaSelect', data);
        })
        .catch(error => {
            console.error('Error loading areas:', error);
        });
}

function populateAreaDropdown(elementId, areas) {
    const dropdown = document.getElementById(elementId);
    dropdown.innerHTML = '';
    
    // Add default option
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Select area (optional)';
    dropdown.appendChild(defaultOption);
    
    areas.forEach(area => {
        const option = document.createElement('option');
        option.value = area.id;
        option.textContent = area.name;
        dropdown.appendChild(option);
    });
}

function populateModelDropdown(elementId, models) {
    const dropdown = document.getElementById(elementId);
    dropdown.innerHTML = '';
    
    models.forEach(model => {
        const option = document.createElement('option');
        option.value = model.id;
        option.textContent = `${model.name} (Accuracy: ${(model.accuracy * 100).toFixed(2)}%)`;
        dropdown.appendChild(option);
    });
}

function populateModelTable(tableBodyId, models) {
    const tableBody = document.getElementById(tableBodyId);
    tableBody.innerHTML = '';
    
    models.forEach(model => {
        const row = document.createElement('tr');
        
        const nameCell = document.createElement('td');
        nameCell.textContent = model.name;
        
        const accuracyCell = document.createElement('td');
        accuracyCell.textContent = `${(model.accuracy * 100).toFixed(2)}%`;
        
        const createdCell = document.createElement('td');
        createdCell.textContent = new Date(model.createdAt).toLocaleString();
        
        row.appendChild(nameCell);
        row.appendChild(accuracyCell);
        row.appendChild(createdCell);
        
        tableBody.appendChild(row);
    });
}

// Employee search and selection functions
function loadEmployees() {
    showLoading();
    
    fetch(`${BASE_URL}/employee`)
        .then(response => response.json())
        .then(data => {
            populateEmployeeTable(data);
            hideLoading();
        })
        .catch(error => {
            console.error('Error loading employees:', error);
            hideLoading();
        });
}

function populateEmployeeTable(employees) {
    const tableBody = document.getElementById('employeeTableBody');
    tableBody.innerHTML = '';
    
    employees.forEach(employee => {
        const row = document.createElement('tr');
        row.setAttribute('data-employee-id', employee.id);
        row.addEventListener('click', () => selectEmployee(employee));
        
        const photoCell = document.createElement('td');
        const photoImg = document.createElement('img');
        photoImg.src = employee.photoUrl || 'https://th.bing.com/th/id/OIP.hlxam_68Up5ge-CcOHvhLQHaHa?w=512&h=512&rs=1&pid=ImgDetMain';
        photoImg.alt = 'Employee Photo';
        photoImg.style.width = '50px';
        photoImg.style.height = '50px';
        photoImg.className = 'rounded-circle';
        photoCell.appendChild(photoImg);
        
        const idCell = document.createElement('td');
        idCell.textContent = employee.id;
        
        const nameCell = document.createElement('td');
        nameCell.textContent = `${employee.fullName}`;
        
        const phoneCell = document.createElement('td');
        phoneCell.textContent = employee.phoneNumber || '-';
        
        row.appendChild(photoCell);
        row.appendChild(idCell);
        row.appendChild(nameCell);
        row.appendChild(phoneCell);
        
        tableBody.appendChild(row);
    });
}

function setupEmployeeSearch() {
    const searchInput = document.getElementById('employeeSearch');
    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        const rows = document.querySelectorAll('#employeeTableBody tr');
        
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

function selectEmployee(employee) {
    selectedEmployee = employee;
    
    document.getElementById('noEmployeeSelected').classList.add('d-none');
    
    const infoCard = document.getElementById('employeeInfoCard');
    infoCard.classList.remove('d-none');
    
    document.getElementById('selectedEmployeePhoto').src = employee.photoUrl || 'https://th.bing.com/th/id/OIP.hlxam_68Up5ge-CcOHvhLQHaHa?w=512&h=512&rs=1&pid=ImgDetMain';
    document.getElementById('selectedEmployeeFullName').textContent = `${employee.fullName}`;
    document.getElementById('selectedEmployeeId').textContent = employee.id;
    document.getElementById('selectedEmployeePhone').textContent = employee.phoneNumber || '-';
    document.getElementById('selectedEmployeeAddress').textContent = employee.address || '-';
    
    // Highlight the selected row
    const rows = document.querySelectorAll('#employeeTableBody tr');
    rows.forEach(row => row.classList.remove('table-primary'));
    
    const selectedRow = document.querySelector(`#employeeTableBody tr[data-employee-id="${employee.id}"]`);
    if (selectedRow) {
        selectedRow.classList.add('table-primary');
    }
}

// File preview functions
function setupFilePreview() {
    // Single file preview for registration
    document.getElementById('fingerprintFile').addEventListener('change', function(event) {
        const previewContainer = document.getElementById('registerPreview');
        previewContainer.innerHTML = '';
        
        if (this.files && this.files[0]) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'img-fluid mt-3 mb-3';
                img.style.maxHeight = '200px';
                img.alt = 'Fingerprint Preview';
                previewContainer.appendChild(img);
                
                // Add position select for the single image
                const positionSelect = document.createElement('select');
                positionSelect.className = 'form-select mt-2';
                positionSelect.id = 'fingerPosition';
                
                const positions = ['LEFT_THUMB', 'LEFT_INDEX', 'LEFT_MIDDLE', 'LEFT_RING', 'LEFT_LITTLE', 
                                  'RIGHT_THUMB', 'RIGHT_INDEX', 'RIGHT_MIDDLE', 'RIGHT_RING', 'RIGHT_LITTLE'];
                
                positions.forEach(pos => {
                    const option = document.createElement('option');
                    option.value = pos;
                    option.textContent = pos.replace('_', ' ');
                    positionSelect.appendChild(option);
                });
                
                previewContainer.appendChild(positionSelect);
            };
            reader.readAsDataURL(this.files[0]);
        }
    });
    
    // Single file preview for recognition
    document.getElementById('fingerprintFileRecognize').addEventListener('change', function(event) {
        const previewContainer = document.getElementById('recognizePreview');
        previewContainer.innerHTML = '';
        
        if (this.files && this.files[0]) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'img-fluid mt-3 mb-3';
                img.style.maxHeight = '200px';
                img.alt = 'Fingerprint Preview';
                previewContainer.appendChild(img);
            };
            reader.readAsDataURL(this.files[0]);
        }
    });
}

// Form submission functions
function setupRegisterForm() {
    document.getElementById('registerForm').addEventListener('submit', function(event) {
        event.preventDefault();
        
        if (!selectedEmployee) {
            alert('Please select an employee first.');
            return;
        }
        
        const fingerprintFile = document.getElementById('fingerprintFile').files[0];
        
        if (!fingerprintFile) {
            alert('Please upload a fingerprint image.');
            return;
        }
        
        const fingerprintPosition = document.getElementById('fingerPosition') ? 
            document.getElementById('fingerPosition').value : 'RIGHT_INDEX';
            
        const segmentationModelId = document.getElementById('segmentationModel').value;
        const recognitionModelId = document.getElementById('recognitionModelRegister').value;
        
        showLoading();
        
        // Create form data
        const formData = new FormData();
        formData.append('file', fingerprintFile);
        formData.append('position', fingerprintPosition);
        formData.append('segmentationModelId', segmentationModelId);
        formData.append('recognitionModelId', recognitionModelId);
        
        // Submit single fingerprint registration
        fetch(`${BASE_URL}/fingerprint-sample/register/${selectedEmployee.id}/single`, {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            hideLoading();
            displayRegistrationResult(data);
        })
        .catch(error => {
            console.error('Error registering fingerprint:', error);
            hideLoading();
            displayRegistrationResult({
                error: 'Failed to register fingerprint. Please try again.'
            });
        });
    });
}

function setupRecognizeForm() {
    const accessTypes = ["ENTRY", "EXIT"];
    const accessTypeSelect = document.getElementById("accessTypeSelect");

    const defaultOption = document.createElement("option");
    defaultOption.value = "ENTRY";
    defaultOption.textContent = "ENTRY";
    accessTypeSelect.appendChild(defaultOption);

    accessTypes.forEach(type => {
        const option = document.createElement("option");
        option.value = type;
        option.textContent = type;
        accessTypeSelect.appendChild(option);
    });
    
    document.getElementById('recognizeForm').addEventListener('submit', function(event) {
        event.preventDefault();
        
        const fingerprintFile = document.getElementById('fingerprintFileRecognize').files[0];
        
        if (!fingerprintFile) {
            alert('Please upload a fingerprint image.');
            return;
        }
        
        const segmentationModelId = document.getElementById('segmentationModelRecognize').value;
        const recognitionModelId = document.getElementById('recognitionModel').value;
        const areaId = document.getElementById('areaSelect').value;
        const accessType = accessTypeSelect.value;

        showLoading();
        
        // Create form data
        const formData = new FormData();
        formData.append('file', fingerprintFile);
        formData.append('segmentationModelId', segmentationModelId);
        formData.append('recognitionModelId', recognitionModelId);
        formData.append('accessType', accessType);
        // formData.append('accessType', accessTypeSelect.value);
        
        // Add area ID if selected
        if (!areaId) {
            alert('Please select area first.');
            return;
        }

        if (areaId) {
            formData.append('areaId', areaId);
        }
        
        // Submit recognition request
        fetch(`${BASE_URL}/fingerprint-recognition/recognize`, {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            hideLoading();
            displayRecognitionResult(data);
        })
        .catch(error => {
            console.error('Error recognizing fingerprint:', error);
            hideLoading();
            displayRecognitionResult({
                error: 'Failed to recognize fingerprint. Please try again.'
            });
        });
    });
}

// Result display functions
function displayRegistrationResult(result) {
    const resultContainer = document.getElementById('registerResultContent');
    
    if (result.error) {
        resultContainer.innerHTML = `
            <div class="alert alert-danger">
                <h5>Registration Failed</h5>
                <p>${result.error}</p>
            </div>
        `;
        return;
    }
    
    resultContainer.innerHTML = `
        <div class="alert alert-success">
            <h5>Registration Successful</h5>
            <p>Successfully registered fingerprint for employee ${result.employeeId}.</p>
        </div>
        <div class="mt-3">
            <strong>Registered Fingerprint:</strong>
            <ul class="list-group mt-2">
                <li class="list-group-item">
                    Position: ${result.position}, Quality: ${(result.quality * 100).toFixed(2)}%, 
                    Captured at: ${new Date(result.capturedAt).toLocaleString()}
                </li>
            </ul>
        </div>
    `;
}

function displayRecognitionResult(result) {
    const resultContainer = document.getElementById('recognizeResultContent');
    
    if (result.error) {
        resultContainer.innerHTML = `
            <div class="alert alert-danger">
                <h5>Recognition Failed</h5>
                <p>${result.error}</p>
            </div>
        `;
        return;
    }
    
    if (result.matched) {
        resultContainer.innerHTML = `
            <div class="alert ${result.authorized ? 'alert-success' : 'alert-warning'}">
                <h5>${result.authorized ? 'Access Granted' : 'Access Denied'}</h5>
                <p>Fingerprint matched with confidence: ${(result.confidence * 100).toFixed(2)}%</p>
            </div>
            
            <div class="card mt-3">
                <div class="card-header">
                    <h5>Matched Employee</h5>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-3 text-center">
                            <img src="${result.employee?.photoUrl || 'https://th.bing.com/th/id/OIP.hlxam_68Up5ge-CcOHvhLQHaHa?w=512&h=512&rs=1&pid=ImgDetMain'}" alt="Employee Photo" 
                                class="img-fluid mb-2" style="max-height: 100px;">
                        </div>
                        <div class="col-md-9">
                            <h5>${result.employee ? `${result.employee.fullName}` : 'Unknown Employee'}</h5>
                            <p class="mb-1"><strong>ID:</strong> ${result.employeeId}</p>
                            <p class="mb-1"><strong>Phone:</strong> ${result.employee?.phoneNumber || '-'}</p>
                            <p class="mb-0"><strong>Address:</strong> ${result.employee?.address || '-'}</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="card mt-3">
                <div class="card-header">
                    <h5>Access Log</h5>
                </div>
                <div class="card-body">
                    <p><strong>Timestamp:</strong> ${new Date(result.accessLog.timestamp).toLocaleString()}</p>
                    <p><strong>Area:</strong> ${result.accessLog.area?.name || 'General Access'}</p>
                    <p><strong>Access Type:</strong> ${result.accessLog.accessType}</p>
                    <p><strong>Authorization:</strong> ${result.authorized ? 'Authorized' : 'Not Authorized'}</p>
                </div>
            </div>
        `;
    } else {
        resultContainer.innerHTML = `
            <div class="alert alert-danger">
                <h5>Access Denied</h5>
                <p>No matching fingerprint found. Confidence: ${(result.confidence * 100).toFixed(2)}%</p>
            </div>
            
            <div class="card mt-3">
                <div class="card-header">
                    <h5>Access Log</h5>
                </div>
                <div class="card-body">
                    <p><strong>Timestamp:</strong> ${new Date(result.accessLog.timestamp).toLocaleString()}</p>
                    <p><strong>Area:</strong> ${result.accessLog.area?.name || 'General Access'}</p>
                    <p><strong>Access Type:</strong> ${result.accessLog.accessType}</p>
                    <p><strong>Authorization:</strong> Not Authorized</p>
                </div>
            </div>
        `;
    }
}

// Utility functions
function showLoading() {
    document.getElementById('loadingIndicator').style.display = 'flex';
}

function hideLoading() {
    document.getElementById('loadingIndicator').style.display = 'none';
}