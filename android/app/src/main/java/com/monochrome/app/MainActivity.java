package com.monochrome.app;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Plugin;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends BridgeActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Register custom native plugins via reflection so compilation
		// does not require direct references to Kotlin classes.
		try {
			Class<?> cls = Class.forName("com.monochrome.app.LocalMusicPlugin");
			if (Plugin.class.isAssignableFrom(cls)) {
				@SuppressWarnings("unchecked")
				Class<? extends Plugin> pluginClass = (Class<? extends Plugin>) cls;
				registerPlugin(pluginClass);
				Log.d("MainActivity", "Registered LocalMusicPlugin via reflection");
			} else {
				Log.w("MainActivity", "Found class but it does not extend Plugin: " + cls.getName());
			}
		} catch (ClassNotFoundException e) {
			Log.w("MainActivity", "LocalMusicPlugin class not found, skipping registration", e);
		} catch (Exception e) {
			Log.e("MainActivity", "Error registering LocalMusicPlugin", e);
		}

		// Log the capacitor.plugins.json contents to help debug plugin exposure.
		try {
			java.io.InputStream is = getAssets().open("capacitor.plugins.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String json = new String(buffer, "UTF-8");
			Log.d("MainActivity", "capacitor.plugins.json: " + json);
		} catch (Exception ex) {
			Log.w("MainActivity", "Could not read capacitor.plugins.json from assets", ex);
		}
	}
}
