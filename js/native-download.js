import { Capacitor } from '@capacitor/core';
import { Filesystem, Directory } from '@capacitor/filesystem';

function blobToBase64(blob) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onerror = () => reject(reader.error);
        reader.onloadend = () => {
            const result = reader.result;
            if (typeof result === 'string') {
                const base64 = result.split(',')[1];
                resolve(base64 || '');
            } else {
                resolve('');
            }
        };
        reader.readAsDataURL(blob);
    });
}

export async function saveBlobToDevice(blob, filename, options = {}) {
    if (!Capacitor.isNativePlatform()) {
        return { saved: false, reason: 'not-native' };
    }

    const platform = Capacitor.getPlatform();
    if (platform !== 'android') {
        return { saved: false, reason: 'unsupported-platform' };
    }

    try {
        await Filesystem.requestPermissions();
    } catch {
        // Ignore permission request failures and attempt write anyway
    }

    const base64 = await blobToBase64(blob);
    const basePath = options.basePath || 'Music/monochrome';
    const path = `${basePath}/${filename}`;

    try {
        await Filesystem.writeFile({
            path,
            data: base64,
            directory: Directory.ExternalStorage,
            recursive: true,
        });
        return { saved: true, path };
    } catch (error) {
        try {
            const fallbackPath = `Monochrome/${filename}`;
            await Filesystem.writeFile({
                path: fallbackPath,
                data: base64,
                directory: Directory.Documents,
                recursive: true,
            });
            return { saved: true, path: fallbackPath, fallback: true };
        } catch (fallbackError) {
            return { saved: false, error: fallbackError };
        }
    }
}
