// downloads-tracker.js
// Tracks ongoing downloads and download history

class DownloadsTracker {
    constructor() {
        this.activeDownloads = new Map(); // id -> download object
        this.downloadHistory = []; // array of completed downloads
        this.listeners = []; // callbacks for UI updates
        this.loadHistory();
    }

    // Add listener for UI updates
    addListener(callback) {
        this.listeners.push(callback);
    }

    // Remove listener
    removeListener(callback) {
        this.listeners = this.listeners.filter(l => l !== callback);
    }

    // Notify all listeners of changes
    notifyListeners() {
        this.listeners.forEach(callback => callback());
    }

    // Start a new download
    startDownload(id, name, artistName) {
        const download = {
            id,
            name,
            artistName,
            progress: 0,
            status: 'downloading', // downloading, completed, failed
            startTime: Date.now(),
            endTime: null,
            fileSize: 0,
            downloadedSize: 0
        };
        this.activeDownloads.set(id, download);
        this.notifyListeners();
        return download;
    }

    // Update download progress
    updateProgress(id, progress, downloadedSize = 0, fileSize = 0) {
        const download = this.activeDownloads.get(id);
        if (download) {
            download.progress = progress;
            if (downloadedSize) download.downloadedSize = downloadedSize;
            if (fileSize) download.fileSize = fileSize;
            this.notifyListeners();
        }
    }

    // Complete a download
    completeDownload(id) {
        const download = this.activeDownloads.get(id);
        if (download) {
            download.status = 'completed';
            download.endTime = Date.now();
            download.progress = 100;

            // Move to history
            this.downloadHistory.unshift(download); // Add to beginning
            this.activeDownloads.delete(id);

            // Keep only last 50 downloads in history
            if (this.downloadHistory.length > 50) {
                this.downloadHistory = this.downloadHistory.slice(0, 50);
            }

            this.saveHistory();
            this.notifyListeners();
        }
    }

    // Mark download as failed
    failDownload(id, error) {
        const download = this.activeDownloads.get(id);
        if (download) {
            download.status = 'failed';
            download.endTime = Date.now();
            download.error = error;

            // Move to history
            this.downloadHistory.unshift(download);
            this.activeDownloads.delete(id);

            this.saveHistory();
            this.notifyListeners();
        }
    }

    // Get all active downloads
    getActiveDownloads() {
        return Array.from(this.activeDownloads.values());
    }

    // Get download history
    getHistory() {
        return this.downloadHistory;
    }

    // Clear download history
    clearHistory() {
        this.downloadHistory = [];
        this.saveHistory();
        this.notifyListeners();
    }

    // Save history to localStorage
    saveHistory() {
        try {
            const historyToSave = this.downloadHistory.slice(0, 50); // Keep max 50
            localStorage.setItem('monochrome_download_history', JSON.stringify(historyToSave));
        } catch (e) {
            console.error('Failed to save download history:', e);
        }
    }

    // Load history from localStorage
    loadHistory() {
        try {
            const saved = localStorage.getItem('monochrome_download_history');
            if (saved) {
                this.downloadHistory = JSON.parse(saved);
            }
        } catch (e) {
            console.error('Failed to load download history:', e);
            this.downloadHistory = [];
        }
    }

    // Format file size in human readable format
    static formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // Format duration in human readable format
    static formatDuration(ms) {
        if (!ms) return '';
        const seconds = Math.floor((ms % 60000) / 1000);
        const minutes = Math.floor((ms % 3600000) / 60000);
        const hours = Math.floor(ms / 3600000);

        if (hours > 0) {
            return `${hours}h ${minutes}m`;
        } else if (minutes > 0) {
            return `${minutes}m ${seconds}s`;
        } else {
            return `${seconds}s`;
        }
    }
}

// Global instance
export const downloadsTracker = new DownloadsTracker();

export default DownloadsTracker;
