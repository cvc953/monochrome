package com.monochrome.app;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.getcapacitor.Bridge; 
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.JSObject;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@CapacitorPlugin(name = "LocalMusic")
public class LocalMusicPlugin extends Plugin {
    private static final String TAG = "LocalMusic";
    private final Set<String> AUDIO_EXTENSIONS = new HashSet<String>() {{
        add("flac"); add("mp3"); add("m4a"); add("wav"); add("aac");
    }};
    private static final int REQUEST_CODE_DIRECTORY = 1001;
    private PluginCall pendingCall = null;
    private int scanProgressCount = 0;

    @PluginMethod
    public void scanMusicDirectory(PluginCall call) {
        File musicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "");
        if (!musicDir.exists()) {
            call.reject("Music directory not found");
            return;
        }
        try {
            ArrayList<JSObject> files = new ArrayList<>();
            scanDirectory(musicDir, files);
            JSObject result = new JSObject();
            result.put("files", new JSONArray(files));
            call.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "Error scanning directory", e);
            call.reject("Error scanning directory: " + e.getMessage());
        }
    }

    @PluginMethod
    public void pickMusicFolder(PluginCall call) {
        this.pendingCall = call;
        Log.d(TAG, "pickMusicFolder called, launching folder picker");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        bridge.getActivity().startActivityForResult(intent, REQUEST_CODE_DIRECTORY);
    }

    @PluginMethod
    public void readFileBytes(PluginCall call) {
        String path = call.getString("path");
        String uriStr = call.getString("uri");
        try {
            byte[] bytes = null;
            if (path != null) {
                File file = new File(path);
                if (!file.exists()) {
                    call.reject("File not found: " + path);
                    return;
                }
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int r;
                while ((r = fis.read(buf)) != -1) baos.write(buf, 0, r);
                fis.close();
                bytes = baos.toByteArray();
            } else if (uriStr != null) {
                Uri uri = Uri.parse(uriStr);
                ContentResolver resolver = bridge.getContext().getContentResolver();
                InputStream is = resolver.openInputStream(uri);
                if (is == null) {
                    call.reject("Unable to open uri: " + uriStr);
                    return;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int r;
                while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
                is.close();
                bytes = baos.toByteArray();
            } else {
                call.reject("Path or uri is required");
                return;
            }

            if (bytes == null) {
                call.reject("Unable to read file bytes");
                return;
            }
            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            JSObject result = new JSObject();
            result.put("data", base64);
            result.put("size", bytes.length);
            call.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "Error reading file", e);
            call.reject("Error reading file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String msg = call.getString("message");
        if (msg == null) msg = "ping";
        Log.d(TAG, "echo called: " + msg);
        JSObject result = new JSObject();
        result.put("message", msg);
        call.resolve(result);
    }

    private void scanDirectory(File dir, ArrayList<JSObject> results) {
        try {
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File file : files) {
                if (file.isDirectory() && !file.getName().startsWith(".")) {
                    scanDirectory(file, results);
                } else if (file.isFile()) {
                    String name = file.getName();
                    String ext = "";
                    int idx = name.lastIndexOf('.');
                    if (idx != -1 && idx + 1 < name.length()) ext = name.substring(idx + 1).toLowerCase();
                    if (AUDIO_EXTENSIONS.contains(ext)) {
                        try {
                            JSObject fileObj = new JSObject();
                            fileObj.put("name", name);
                            fileObj.put("path", file.getAbsolutePath());
                            fileObj.put("size", file.length());
                            fileObj.put("lastModified", file.lastModified());
                            results.add(fileObj);
                            Log.d(TAG, "Found audio file: " + name);
                            // Progress notification
                            try {
                                scanProgressCount++;
                                if ((scanProgressCount % 50) == 0) {
                                    JSObject ev = new JSObject();
                                    ev.put("count", scanProgressCount);
                                    notifyListeners("scanProgress", ev);
                                }
                            } catch (Exception ignored) {}
                        } catch (Exception ex) {
                            Log.e(TAG, "Error adding file to results", ex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scanning directory: " + dir.getAbsolutePath(), e);
        }
    }

    private void scanDocumentFile(DocumentFile dir, ArrayList<JSObject> results) {
        try {
            DocumentFile[] children = dir.listFiles();
            for (DocumentFile child : children) {
                if (child.isDirectory()) {
                    if (child.getName() != null && !child.getName().startsWith(".")) {
                        scanDocumentFile(child, results);
                    }
                } else if (child.isFile()) {
                    String name = child.getName();
                    if (name == null) continue;
                    String ext = "";
                    int idx = name.lastIndexOf('.');
                    if (idx != -1 && idx + 1 < name.length()) ext = name.substring(idx + 1).toLowerCase();
                    if (AUDIO_EXTENSIONS.contains(ext)) {
                        try {
                            JSObject fileObj = new JSObject();
                            fileObj.put("name", name);
                            fileObj.put("uri", child.getUri().toString());
                            try { fileObj.put("size", child.length()); } catch (Exception ignored) {}
                            results.add(fileObj);
                            Log.d(TAG, "Found audio file (SAF): " + name);
                                // Progress notification
                                try {
                                    scanProgressCount++;
                                    if ((scanProgressCount % 50) == 0) {
                                        JSObject ev = new JSObject();
                                        ev.put("count", scanProgressCount);
                                        notifyListeners("scanProgress", ev);
                                    }
                                } catch (Exception ignored) {}
                        } catch (Exception ex) {
                            Log.e(TAG, "Error adding SAF file to results", ex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scanning DocumentFile directory", e);
        }
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DIRECTORY && data != null) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                try {
                    Log.d(TAG, "handleOnActivityResult: treeUri=" + treeUri);
                    try {
                        // Notify JS that scanning is about to start
                        scanProgressCount = 0;
                        JSObject startEv = new JSObject();
                        startEv.put("uri", treeUri.toString());
                        notifyListeners("scanStarted", startEv);
                    } catch (Exception ignored) {}
                    try {
                        ContentResolver resolver = getContext().getContentResolver();
                        resolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } catch (Exception permEx) {
                        Log.w(TAG, "Could not take persistable permission", permEx);
                    }

                    String path = getPathFromUri(treeUri);
                    Log.d(TAG, "Resolved path from uri: " + path);
                    ArrayList<JSObject> files = new ArrayList<>();
                    if (path != null) {
                        scanDirectory(new File(path), files);
                        JSObject result = new JSObject();
                        result.put("files", new JSONArray(files));
                        result.put("path", path);
                        try {
                            JSObject doneEv = new JSObject();
                            doneEv.put("count", files.size());
                            notifyListeners("scanCompleted", doneEv);
                        } catch (Exception ignored) {}
                        if (pendingCall != null) pendingCall.resolve(result);
                    } else {
                        DocumentFile docFile = DocumentFile.fromTreeUri(getContext(), treeUri);
                        if (docFile != null && docFile.isDirectory()) {
                            scanDocumentFile(docFile, files);
                            JSObject result = new JSObject();
                            result.put("files", new JSONArray(files));
                            result.put("uri", treeUri.toString());
                            result.put("count", files.size());
                            Log.d(TAG, "SAF scan found " + files.size() + " files");
                            try {
                                JSObject doneEv = new JSObject();
                                doneEv.put("count", files.size());
                                notifyListeners("scanCompleted", doneEv);
                            } catch (Exception ignored) {}
                            if (pendingCall != null) pendingCall.resolve(result);
                        } else {
                            if (pendingCall != null) pendingCall.reject("Could not access selected folder");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing folder selection", e);
                    if (pendingCall != null) pendingCall.reject("Error: " + e.getMessage());
                }
            }
            pendingCall = null;
        }
    }

    private String getPathFromUri(Uri uri) {
        if (uri == null) return null;
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) return uri.getPath();
        if ("content".equals(scheme)) {
            try {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                if (split.length >= 2) {
                    if ("primary".equals(split[0])) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } else {
                        return "/storage/" + split[0] + "/" + split[1];
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}
