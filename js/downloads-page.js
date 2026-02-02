// downloads-page.js
// UI for the downloads page

import { downloadsTracker } from './downloads-tracker.js';

export function initDownloadsPage() {
    const activeTab = document.querySelector('.downloads-tab[data-tab="active"]');
    const historyTab = document.querySelector('.downloads-tab[data-tab="history"]');
    const activeContent = document.getElementById('downloads-active');
    const historyContent = document.getElementById('downloads-history');
    const clearHistoryBtn = document.getElementById('clear-history-btn');

    // Tab switching
    if (activeTab) {
        activeTab.addEventListener('click', () => {
            activeTab.classList.add('active');
            historyTab.classList.remove('active');
            activeContent.style.display = 'block';
            historyContent.style.display = 'none';
        });
    }

    if (historyTab) {
        historyTab.addEventListener('click', () => {
            historyTab.classList.add('active');
            activeTab.classList.remove('active');
            activeContent.style.display = 'none';
            historyContent.style.display = 'block';
            renderHistory();
        });
    }

    // Clear history button
    if (clearHistoryBtn) {
        clearHistoryBtn.addEventListener('click', () => {
            if (confirm('Are you sure you want to clear download history?')) {
                downloadsTracker.clearHistory();
                renderHistory();
            }
        });
    }

    // Listen for download tracker changes
    downloadsTracker.addListener(() => {
        renderActive();
    });

    // Initial render
    renderActive();
}

function renderActive() {
    const list = document.getElementById('downloads-active-list');
    const activeDownloads = downloadsTracker.getActiveDownloads();

    if (activeDownloads.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="7 10 12 15 17 10" />
                    <line x1="12" x2="12" y1="15" y2="3" />
                </svg>
                <p>No active downloads</p>
            </div>
        `;
        return;
    }

    list.innerHTML = activeDownloads.map(download => `
        <div class="download-item" data-id="${download.id}">
            <div class="download-info">
                <div class="download-title">${escapeHtml(download.name)}</div>
                ${download.artistName ? `<div class="download-artist">${escapeHtml(download.artistName)}</div>` : ''}
            </div>
            <div class="download-progress">
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${download.progress}%"></div>
                </div>
                <div class="progress-text">${Math.round(download.progress)}%</div>
            </div>
            ${download.fileSize ? `<div class="download-size">${downloadsTracker.constructor.formatFileSize(download.downloadedSize)} / ${downloadsTracker.constructor.formatFileSize(download.fileSize)}</div>` : ''}
        </div>
    `).join('');
}

function renderHistory() {
    const list = document.getElementById('downloads-history-list');
    const clearBtn = document.getElementById('clear-history-btn');
    const history = downloadsTracker.getHistory();

    if (history.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10" />
                    <polyline points="12 6 12 12 16 14" />
                </svg>
                <p>No download history</p>
            </div>
        `;
        clearBtn.style.display = 'none';
        return;
    }

    clearBtn.style.display = 'block';
    list.innerHTML = history.map(download => {
        const statusClass = download.status === 'completed' ? 'success' : 'error';
        const statusText = download.status === 'completed' ? 'Completed' : 'Failed';
        const duration = download.endTime ? downloadsTracker.constructor.formatDuration(download.endTime - download.startTime) : '';

        return `
            <div class="download-item download-item-${download.status}" data-id="${download.id}">
                <div class="download-info">
                    <div class="download-title">${escapeHtml(download.name)}</div>
                    ${download.artistName ? `<div class="download-artist">${escapeHtml(download.artistName)}</div>` : ''}
                </div>
                <div class="download-status">
                    <span class="status-badge ${statusClass}">${statusText}</span>
                    ${duration ? `<span class="download-time">${duration}</span>` : ''}
                </div>
                ${download.fileSize ? `<div class="download-size">${downloadsTracker.constructor.formatFileSize(download.fileSize)}</div>` : ''}
            </div>
        `;
    }).join('');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

export default initDownloadsPage;
