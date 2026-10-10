/**
 * ==============================================================================
 * SmartFind - AI-Based Smart Lost & Found System
 * Unified Frontend JavaScript Architecture
 * Connects to Spring Boot backend at http://localhost:8083/api
 * ==============================================================================
 */

// Automatically resolves backend URL for Localhost, Spring Boot, or Vercel -> Render deployment
function getApiBaseUrl() {
    if (typeof window === "undefined") return "http://localhost:8083/api";
    
    const customUrl = localStorage.getItem("smartfind_backend_url");
    if (customUrl) return customUrl.trim().replace(/\/+$/, "");

    if (window.location.port === "8083") return "/api";

    if (window.location.hostname.includes("vercel.app") || window.location.hostname.includes("github.io")) {
        return "https://smartfind-backend.onrender.com/api";
    }

    return "http://localhost:8083/api";
}

const API_BASE = getApiBaseUrl();

function setCustomBackendUrl(url) {
    if (url) {
        localStorage.setItem("smartfind_backend_url", url);
    } else {
        localStorage.removeItem("smartfind_backend_url");
    }
    window.location.reload();
}

// ------------------------------------------------------------------------------
// 1. Session & Authentication Management
// ------------------------------------------------------------------------------

function getLoggedInUser() {
    try {
        const stored = localStorage.getItem("smartfind_user");
        return stored ? JSON.parse(stored) : null;
    } catch (e) {
        console.error("Failed to parse user session", e);
        return null;
    }
}

function setLoggedInUser(user) {
    localStorage.setItem("smartfind_user", JSON.stringify(user));
}

function clearSession() {
    localStorage.removeItem("smartfind_user");
}

function requireAuth() {
    const user = getLoggedInUser();
    if (!user) {
        window.location.href = "login.html";
        return null;
    }
    return user;
}

function requireAdmin() {
    const user = requireAuth();
    if (!user) return null;
    if (user.role !== "ADMIN") {
        alert("Access Denied: Administrator privileges are required to view this page.");
        window.location.href = "index.html";
        return null;
    }
    return user;
}

async function handleLogout() {
    const user = getLoggedInUser();
    try {
        if (user) {
            await fetch(`${API_BASE}/users/logout?userId=${user.id}`, { method: "POST" });
        }
    } catch (err) {
        console.warn("Logout request completed locally", err);
    } finally {
        clearSession();
        window.location.href = "login.html";
    }
}

// ------------------------------------------------------------------------------
// 2. Navigation Bar Rendering
// ------------------------------------------------------------------------------

function initNavigation(activePage = "") {
    const navPlaceholder = document.getElementById("navbar-placeholder");
    if (!navPlaceholder) return;

    const user = getLoggedInUser();
    const isAdmin = user && user.role === "ADMIN";

    let authLinksHtml = "";
    if (user) {
        authLinksHtml = `
            <div class="user-session-panel">
                <span class="user-greeting">👋 ${escapeHtml(user.name)}</span>
                <span class="user-role-badge ${isAdmin ? 'admin' : ''}">${user.role}</span>
                <button class="btn btn-secondary btn-sm" onclick="handleLogout()" title="Logout of system">Logout</button>
            </div>
        `;
    } else {
        authLinksHtml = `
            <div class="user-session-panel">
                <a href="login.html" class="btn btn-secondary btn-sm">Login</a>
                <a href="register.html" class="btn btn-primary btn-sm">Register</a>
            </div>
        `;
    }

    const adminNavHtml = isAdmin ? `
        <li><a href="admin.html" class="${activePage === 'admin' ? 'active' : ''}">Admin Panel</a></li>
    ` : "";

    navPlaceholder.innerHTML = `
        <nav class="navbar">
            <div class="nav-container">
                <a href="index.html" class="brand-link">
                    <span class="brand-badge">SF</span>
                    <span>SmartFind</span>
                </a>
                <ul class="nav-links">
                    <li><a href="index.html" class="${activePage === 'dashboard' ? 'active' : ''}">Dashboard</a></li>
                    <li><a href="lost-item.html" class="${activePage === 'lost' ? 'active' : ''}">Report Lost</a></li>
                    <li><a href="found-item.html" class="${activePage === 'found' ? 'active' : ''}">Report Found</a></li>
                    <li><a href="search.html" class="${activePage === 'search' ? 'active' : ''}">Search</a></li>
                    <li><a href="my-reports.html" class="${activePage === 'my-reports' ? 'active' : ''}">My Reports</a></li>
                    ${adminNavHtml}
                </ul>
                ${authLinksHtml}
            </div>
        </nav>
    `;
}

// ------------------------------------------------------------------------------
// 3. Centralized API Request Handler
// ------------------------------------------------------------------------------

async function apiRequest(endpoint, options = {}) {
    const user = getLoggedInUser();
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (user) {
        headers["X-User-Id"] = user.id;
        headers["X-User-Role"] = user.role;
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            ...options,
            headers
        });

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            const errorMsg = data && data.message ? data.message : `Server Error: HTTP ${response.status}`;
            throw new Error(errorMsg);
        }

        return data;
    } catch (err) {
        if (err.name === "TypeError" && err.message.includes("fetch")) {
            throw new Error("Unable to connect to SmartFind backend at http://localhost:8083. Please ensure Spring Boot is running in STS.");
        }
        throw err;
    }
}

// ------------------------------------------------------------------------------
// 4. Alert & UI Utilities
// ------------------------------------------------------------------------------

function showAlert(containerId, message, type = "danger") {
    const container = document.getElementById(containerId);
    if (!container) return;

    const icon = type === "success" ? "✅" : (type === "warning" ? "⚠️" : (type === "info" ? "ℹ️" : "❌"));
    container.innerHTML = `
        <div class="alert alert-${type}">
            <span>${icon}</span>
            <div>${escapeHtml(message)}</div>
        </div>
    `;
}

function clearAlert(containerId) {
    const container = document.getElementById(containerId);
    if (container) container.innerHTML = "";
}

function escapeHtml(str) {
    if (!str) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function getBadgeHtml(reportType) {
    if (reportType === "LOST") {
        return `<span class="badge badge-lost">LOST</span>`;
    } else {
        return `<span class="badge badge-found">FOUND</span>`;
    }
}

// ------------------------------------------------------------------------------
// 5. Page Specific Logic
// ------------------------------------------------------------------------------

// --- Dashboard Logic (index.html) ---
let currentAllItems = [];
let selectedLostItemId = null;

async function loadDashboard() {
    initNavigation("dashboard");
    const user = getLoggedInUser();

    await refreshDashboardStats();
    await loadRecentReports();

    // Listen for filter / quick search in dashboard table
    const searchInput = document.getElementById("dash-search-input");
    const filterSelect = document.getElementById("dash-filter-type");

    if (searchInput) {
        searchInput.addEventListener("input", filterDashboardTable);
    }
    if (filterSelect) {
        filterSelect.addEventListener("change", filterDashboardTable);
    }
}

async function refreshDashboardStats() {
    try {
        const response = await apiRequest("/items/stats");
        if (response && response.success && response.data) {
            const stats = response.data;
            document.getElementById("stat-total-reports").textContent = stats.totalReports ?? 0;
            document.getElementById("stat-total-lost").textContent = stats.totalLost ?? 0;
            document.getElementById("stat-total-found").textContent = stats.totalFound ?? 0;
            document.getElementById("stat-potential-matches").textContent = stats.potentialMatches ?? 0;
        }
    } catch (err) {
        console.error("Failed to load dashboard stats", err);
        showAlert("dash-alert-container", err.message, "danger");
    }
}

async function loadRecentReports() {
    const tbody = document.getElementById("reports-table-body");
    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 2rem;">Loading latest reports from database...</td></tr>`;

    try {
        const response = await apiRequest("/items/all");
        if (response && response.success) {
            currentAllItems = response.data || [];
            renderReportsTable(currentAllItems);
        }
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: #dc2626; padding: 2rem;">${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderReportsTable(items) {
    const tbody = document.getElementById("reports-table-body");
    if (!tbody) return;

    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 2rem; color: #64748b;">No item reports found. Click "Report Lost" or "Report Found" above to submit one.</td></tr>`;
        return;
    }

    tbody.innerHTML = items.map(item => `
        <tr onclick="selectReportItem(${item.id})" id="row-item-${item.id}" class="${selectedLostItemId === item.id ? 'selected-row' : ''}">
            <td><strong>#${item.id}</strong></td>
            <td><strong>${escapeHtml(item.itemName)}</strong></td>
            <td>${getBadgeHtml(item.reportType)}</td>
            <td>📍 ${escapeHtml(item.location)}</td>
            <td>📅 ${item.itemDate}</td>
            <td>
                ${item.reportType === 'LOST' 
                    ? `<button class="btn btn-primary btn-sm" onclick="event.stopPropagation(); triggerMatchForLostItem(${item.id})">🔍 Match</button>` 
                    : `<button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); viewItemDetails(${item.id})">Details</button>`}
            </td>
        </tr>
    `).join("");
}

function filterDashboardTable() {
    const query = (document.getElementById("dash-search-input")?.value || "").toLowerCase().trim();
    const typeFilter = document.getElementById("dash-filter-type")?.value || "";

    const filtered = currentAllItems.filter(item => {
        const matchesQuery = !query || 
            item.itemName.toLowerCase().includes(query) || 
            item.description.toLowerCase().includes(query) ||
            item.location.toLowerCase().includes(query) ||
            String(item.id).includes(query);
        const matchesType = !typeFilter || item.reportType === typeFilter;
        return matchesQuery && matchesType;
    });

    renderReportsTable(filtered);
}

function selectReportItem(id) {
    const item = currentAllItems.find(i => i.id === id);
    if (!item) return;

    // Highlight row
    document.querySelectorAll("#reports-table-body tr").forEach(tr => tr.classList.remove("selected-row"));
    const row = document.getElementById(`row-item-${id}`);
    if (row) row.classList.add("selected-row");

    // Display in Details Panel
    displayItemInDetailsPanel(item);

    // If it's a LOST item, automatically populate the matching input field
    if (item.reportType === "LOST") {
        selectedLostItemId = item.id;
        const matchInput = document.getElementById("match-lost-id-input");
        if (matchInput) {
            matchInput.value = item.id;
        }
    }
}

function viewItemDetails(id) {
    selectReportItem(id);
}

function displayItemInDetailsPanel(item) {
    const detailsContainer = document.getElementById("item-details-content");
    if (!detailsContainer) return;

    detailsContainer.innerHTML = `
        <div class="details-row">
            <span class="details-label">Report ID:</span>
            <span class="details-value">#${item.id}</span>
        </div>
        <div class="details-row">
            <span class="details-label">Item Name:</span>
            <span class="details-value"><strong>${escapeHtml(item.itemName)}</strong></span>
        </div>
        <div class="details-row">
            <span class="details-label">Type:</span>
            <span class="details-value">${getBadgeHtml(item.reportType)}</span>
        </div>
        <div class="details-row">
            <span class="details-label">Location:</span>
            <span class="details-value">📍 ${escapeHtml(item.location)}</span>
        </div>
        <div class="details-row">
            <span class="details-label">Date:</span>
            <span class="details-value">📅 ${item.itemDate}</span>
        </div>
        <div class="details-row">
            <span class="details-label">Description:</span>
            <span class="details-value">${escapeHtml(item.description)}</span>
        </div>
        <div class="details-row">
            <span class="details-label">Reported By:</span>
            <span class="details-value">${escapeHtml(item.userName || "Student")} (${escapeHtml(item.userEmail || "N/A")})</span>
        </div>
        ${item.reportType === 'LOST' ? `
            <div style="margin-top: 1rem;">
                <button class="btn btn-primary btn-block btn-sm" onclick="triggerMatchForLostItem(${item.id})">
                    ⚡ Run AI Matching for this Item
                </button>
            </div>
        ` : ''}
    `;
}

function triggerMatchForLostItem(id) {
    selectedLostItemId = id;
    const matchInput = document.getElementById("match-lost-id-input");
    if (matchInput) {
        matchInput.value = id;
    }
    runMatching();
}

async function runMatching() {
    const input = document.getElementById("match-lost-id-input");
    const resultsContainer = document.getElementById("matching-results-container");
    if (!input || !resultsContainer) return;

    const lostItemId = input.value.trim();
    if (!lostItemId) {
        showAlert("match-alert-container", "Please enter or select a valid Lost Report ID first.", "warning");
        return;
    }

    clearAlert("match-alert-container");
    resultsContainer.innerHTML = `
        <div style="text-align: center; padding: 2rem;">
            <p>🤖 Running AI Matching Engine...</p>
            <p style="font-size: 0.85rem; color: #64748b;">Evaluating item name, description keywords, location, and date proximity...</p>
        </div>
    `;

    try {
        const response = await apiRequest(`/matching/${lostItemId}`);
        if (!response || !response.success || !response.data) {
            throw new Error(response?.message || "Failed to retrieve matching results.");
        }

        const matchData = response.data;
        const matches = matchData.matches || [];

        if (matches.length === 0) {
            resultsContainer.innerHTML = `
                <div class="alert alert-info">
                    <span>ℹ️</span>
                    <div>
                        <strong>No matches found above 60% threshold.</strong><br>
                        ${escapeHtml(matchData.message)}
                    </div>
                </div>
            `;
            return;
        }

        resultsContainer.innerHTML = `
            <div style="margin-bottom: 0.9rem; font-size: 0.88rem; color: #166534; background: #dcfce7; padding: 0.6rem 0.9rem; border-radius: 6px;">
                🎯 <strong>${matches.length} Potential Match(es) Found</strong> for "${escapeHtml(matchData.lostItemName)}":
            </div>
            ${matches.map(m => `
                <div class="match-card">
                    <div class="match-card-header">
                        <div>
                            <strong style="font-size: 1.05rem;">${escapeHtml(m.itemName)}</strong>
                            <div style="font-size: 0.8rem; color: #64748b;">Found Report #${m.foundReportId} | 📅 ${m.date}</div>
                        </div>
                        <span class="match-score-pill ${m.matchScore >= 80 ? 'score-high' : 'score-medium'}">
                            ⭐ ${m.matchScore}% Match
                        </span>
                    </div>
                    <p style="font-size: 0.86rem; color: #334155; margin: 0.4rem 0;">${escapeHtml(m.description)}</p>
                    <div style="font-size: 0.83rem; color: #64748b;">📍 Location: <strong>${escapeHtml(m.location)}</strong></div>
                    <div class="match-explanation">
                        <strong>AI Scoring Breakdown:</strong><br>
                        ${escapeHtml(m.matchExplanation)}
                    </div>
                    <div style="margin-top: 0.6rem; font-size: 0.82rem; color: #4338ca;">
                        👤 Found by: <strong>${escapeHtml(m.contactName)}</strong> (${escapeHtml(m.contactEmail)})
                    </div>
                </div>
            `).join("")}
        `;

        // Refresh stats card as well
        refreshDashboardStats();

    } catch (err) {
        resultsContainer.innerHTML = "";
        showAlert("match-alert-container", err.message, "danger");
    }
}

// --- Item Reporting Logic (lost-item.html & found-item.html) ---
async function handleItemReportSubmit(event, reportType) {
    event.preventDefault();

    const user = requireAuth();
    if (!user) return;

    const itemName = document.getElementById("item-name").value.trim();
    const description = document.getElementById("item-description").value.trim();
    const location = document.getElementById("item-location").value.trim();
    const itemDate = document.getElementById("item-date").value;

    const alertContainer = "report-alert-container";
    clearAlert(alertContainer);

    if (!itemName || !description || !location || !itemDate) {
        showAlert(alertContainer, "All fields are required. Please fill out the entire form.", "warning");
        return;
    }

    const payload = {
        itemName,
        description,
        location,
        itemDate,
        reportType,
        userId: user.id
    };

    const submitBtn = document.getElementById("submit-btn");
    if (submitBtn) submitBtn.disabled = true;

    try {
        const response = await apiRequest("/items/add", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        if (response && response.success) {
            showAlert(alertContainer, response.message, "success");
            document.getElementById("item-form").reset();
            setTimeout(() => {
                window.location.href = "index.html";
            }, 1600);
        }
    } catch (err) {
        showAlert(alertContainer, err.message, "danger");
    } finally {
        if (submitBtn) submitBtn.disabled = false;
    }
}

// --- Search Logic (search.html) ---
async function initSearchPage() {
    initNavigation("search");
    loadAllSearchItems();

    const searchBtn = document.getElementById("search-btn");
    const queryInput = document.getElementById("search-query");
    const typeSelect = document.getElementById("search-type");
    const locInput = document.getElementById("search-location");

    if (searchBtn) {
        searchBtn.addEventListener("click", () => executeSearch());
    }
    if (queryInput) {
        queryInput.addEventListener("keyup", (e) => { if (e.key === "Enter") executeSearch(); });
    }
}

async function loadAllSearchItems() {
    const resultsContainer = document.getElementById("search-results-table");
    if (!resultsContainer) return;

    try {
        const response = await apiRequest("/items/all");
        if (response && response.success) {
            renderSearchResults(response.data || []);
        }
    } catch (err) {
        showAlert("search-alert-container", err.message, "danger");
    }
}

async function executeSearch() {
    const query = document.getElementById("search-query")?.value.trim() || "";
    const type = document.getElementById("search-type")?.value || "";
    const location = document.getElementById("search-location")?.value.trim() || "";

    clearAlert("search-alert-container");

    const params = new URLSearchParams();
    if (query) params.append("query", query);
    if (type) params.append("reportType", type);
    if (location) params.append("location", location);

    const tbody = document.getElementById("search-results-table");
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2rem;">Searching items...</td></tr>`;

    try {
        const response = await apiRequest(`/items/search?${params.toString()}`);
        if (response && response.success) {
            renderSearchResults(response.data || []);
        }
    } catch (err) {
        showAlert("search-alert-container", err.message, "danger");
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: #dc2626; padding: 2rem;">${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderSearchResults(items) {
    const tbody = document.getElementById("search-results-table");
    const countLabel = document.getElementById("search-count-label");
    if (!tbody) return;

    if (countLabel) {
        countLabel.textContent = `Showing ${items.length} report(s)`;
    }

    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2.5rem; color: #64748b;">No matching reports found. Try refining your search query or filters.</td></tr>`;
        return;
    }

    tbody.innerHTML = items.map(item => `
        <tr>
            <td><strong>#${item.id}</strong></td>
            <td><strong>${escapeHtml(item.itemName)}</strong></td>
            <td>${getBadgeHtml(item.reportType)}</td>
            <td>📍 ${escapeHtml(item.location)}</td>
            <td>📅 ${item.itemDate}</td>
            <td style="max-width: 250px; font-size: 0.85rem;">${escapeHtml(item.description)}</td>
            <td>
                ${item.reportType === 'LOST' 
                    ? `<a href="index.html" class="btn btn-primary btn-sm" onclick="sessionStorage.setItem('match_id', ${item.id})">⚡ Match</a>` 
                    : `<span style="font-size: 0.82rem; color: #64748b;">Found report</span>`}
            </td>
        </tr>
    `).join("");
}

// --- My Reports Logic (my-reports.html) ---
async function initMyReportsPage() {
    initNavigation("my-reports");
    const user = requireAuth();
    if (!user) return;

    loadMyReports(user.id);
}

async function loadMyReports(userId) {
    const tbody = document.getElementById("my-reports-table");
    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 2rem;">Loading your reports...</td></tr>`;

    try {
        const response = await apiRequest(`/items/my-reports/${userId}`);
        if (response && response.success) {
            renderMyReports(response.data || []);
        }
    } catch (err) {
        showAlert("my-reports-alert", err.message, "danger");
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: #dc2626; padding: 2rem;">${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderMyReports(reports) {
    const tbody = document.getElementById("my-reports-table");
    if (!tbody) return;

    if (!reports || reports.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 2.5rem; color: #64748b;">You have not submitted any reports yet. Use "Report Lost" or "Report Found" to report an item.</td></tr>`;
        return;
    }

    tbody.innerHTML = reports.map(r => `
        <tr>
            <td><strong>#${r.id}</strong></td>
            <td><strong>${escapeHtml(r.itemName)}</strong></td>
            <td>${getBadgeHtml(r.reportType)}</td>
            <td>📍 ${escapeHtml(r.location)}</td>
            <td>📅 ${r.itemDate}</td>
            <td>
                <button class="btn btn-outline-danger btn-sm" onclick="deleteMyReport(${r.id})">🗑️ Delete</button>
            </td>
        </tr>
    `).join("");
}

async function deleteMyReport(reportId) {
    const user = requireAuth();
    if (!user) return;

    if (!confirm(`Are you sure you want to delete report #${reportId}? This action cannot be undone.`)) {
        return;
    }

    clearAlert("my-reports-alert");

    try {
        const response = await apiRequest(`/items/${reportId}?userId=${user.id}`, {
            method: "DELETE"
        });

        if (response && response.success) {
            showAlert("my-reports-alert", response.message, "success");
            loadMyReports(user.id);
        }
    } catch (err) {
        showAlert("my-reports-alert", err.message, "danger");
    }
}

// --- Admin Panel Logic (admin.html) ---
async function initAdminPage() {
    initNavigation("admin");
    const user = requireAdmin();
    if (!user) return;

    loadAdminReports();

    const filterSelect = document.getElementById("admin-filter-type");
    if (filterSelect) {
        filterSelect.addEventListener("change", () => loadAdminReports());
    }
}

async function loadAdminReports() {
    const user = requireAdmin();
    if (!user) return;

    const tbody = document.getElementById("admin-reports-table");
    const filter = document.getElementById("admin-filter-type")?.value || "";
    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2rem;">Loading administrative reports...</td></tr>`;

    let url = `/admin/items?adminUserId=${user.id}`;
    if (filter) {
        url += `&reportType=${filter}`;
    }

    try {
        const response = await apiRequest(url);
        if (response && response.success) {
            renderAdminReports(response.data || []);
        }
    } catch (err) {
        showAlert("admin-alert", err.message, "danger");
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: #dc2626; padding: 2rem;">${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderAdminReports(reports) {
    const tbody = document.getElementById("admin-reports-table");
    if (!tbody) return;

    if (!reports || reports.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2.5rem; color: #64748b;">No reports found matching the selected administrative filter.</td></tr>`;
        return;
    }

    tbody.innerHTML = reports.map(r => `
        <tr>
            <td><strong>#${r.id}</strong></td>
            <td><strong>${escapeHtml(r.itemName)}</strong></td>
            <td>${getBadgeHtml(r.reportType)}</td>
            <td>📍 ${escapeHtml(r.location)}</td>
            <td>📅 ${r.itemDate}</td>
            <td>${escapeHtml(r.userName || "Unknown")} (${escapeHtml(r.userEmail || "N/A")})</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="adminDeleteReport(${r.id})">Remove Report</button>
            </td>
        </tr>
    `).join("");
}

async function adminDeleteReport(reportId) {
    const user = requireAdmin();
    if (!user) return;

    if (!confirm(`Administrator Confirmation: Are you sure you want to permanently delete report #${reportId}?`)) {
        return;
    }

    clearAlert("admin-alert");

    try {
        const response = await apiRequest(`/admin/items/${reportId}?adminUserId=${user.id}`, {
            method: "DELETE"
        });

        if (response && response.success) {
            showAlert("admin-alert", response.message, "success");
            loadAdminReports();
        }
    } catch (err) {
        showAlert("admin-alert", err.message, "danger");
    }
}

// --- Authentication Logic (login.html & register.html) ---
async function handleLoginSubmit(event) {
    event.preventDefault();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    clearAlert("login-alert");

    if (!email || !password) {
        showAlert("login-alert", "Please enter both email and password.", "warning");
        return;
    }

    const submitBtn = document.getElementById("login-btn");
    if (submitBtn) submitBtn.disabled = true;

    try {
        const response = await apiRequest("/users/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });

        if (response && response.success && response.data) {
            setLoggedInUser(response.data);
            showAlert("login-alert", "Login successful! Redirecting to dashboard...", "success");
            setTimeout(() => {
                window.location.href = "index.html";
            }, 800);
        }
    } catch (err) {
        showAlert("login-alert", err.message, "danger");
    } finally {
        if (submitBtn) submitBtn.disabled = false;
    }
}

async function handleRegisterSubmit(event) {
    event.preventDefault();
    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;
    const role = document.getElementById("role") ? document.getElementById("role").value : "USER";

    clearAlert("register-alert");

    if (!name || !email || !password) {
        showAlert("register-alert", "All fields are required.", "warning");
        return;
    }

    if (password !== confirmPassword) {
        showAlert("register-alert", "Passwords do not match. Please verify and try again.", "warning");
        return;
    }

    if (password.length < 6) {
        showAlert("register-alert", "Password must be at least 6 characters long.", "warning");
        return;
    }

    const submitBtn = document.getElementById("register-btn");
    if (submitBtn) submitBtn.disabled = true;

    try {
        const response = await apiRequest("/users/register", {
            method: "POST",
            body: JSON.stringify({ name, email, password, role })
        });

        if (response && response.success && response.data) {
            setLoggedInUser(response.data);
            showAlert("register-alert", "Registration successful! Welcome to SmartFind.", "success");
            setTimeout(() => {
                window.location.href = "index.html";
            }, 1000);
        }
    } catch (err) {
        showAlert("register-alert", err.message, "danger");
    } finally {
        if (submitBtn) submitBtn.disabled = false;
    }
}
