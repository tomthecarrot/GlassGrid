// See README and LICENSE files for more information.

package com.carrotcorp.glassgrid;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MainActivity extends Activity implements Callback {
	Camera camera;
	WakeLock wl;
	boolean cameraButtonCalledPause;
	boolean createCalledResume = true;
	
	
	///// Activity methods /////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Set up the view:
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Open the camera:
		openCamera();
		
		// Set up and acquire a wake lock so the user doesn't have to keep
		// tapping the side of their head every time the display dims:
		PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "com.carrotcorp.glassgrid");
		wl.acquire();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// If the camera button is pressed, both onPause() and onKeyDown() will call releaseAll(),
		// resulting in a crash. Prevent a crash by restricting onPause() from calling releaseAll()
		// if it is already being called by onKeyDown():
		if (!cameraButtonCalledPause) {
			// Release all resources from GlassGrid:
			release(true);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// If onResume() was called when exiting a photo capture, release all resources from GlassGrid:
		if (!createCalledResume) {
			release(true);
		}
	}
	
	// If the camera button is pressed, release the camera from GlassGrid
	// to let the system use it. Then, exit GlassGrid:
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_CAMERA) {
	    	// Prevent onPause() from calling release() also:
	    	cameraButtonCalledPause = true;
	    	createCalledResume = false;
	    	
	    	// Release main resources from GlassGrid:
	        release(false);
	        
	        return false;
	    } else {
	        return super.onKeyDown(keyCode, event);
	    }
	}
	
	private void release(boolean all) {
		// Release the camera for other apps and the system to use, if it has been initialized:
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}

		// Release the wake lock:
		wl.release();

		// Finish this activity:
		finish();
	}
	
	///// Camera methods /////
	
	private void openCamera() {
		// Get the designated SurfaceView and its SurfaceHolder for camera preview display:
		SurfaceView cameraview = (SurfaceView) findViewById(R.id.cameraview);
		SurfaceHolder cameraholder = cameraview.getHolder();
		cameraholder.addCallback(this);
		cameraholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// If the camera is not already set up, set it up.
		if (camera == null) {
			// Open the camera object:
			camera = Camera.open();
			
			// There is currently a bug in the Glass camera system in which the camera output will be glitched.
			// Fix it with some explicit parameters:
			fixCamera();
			
			// Set the camera preview display to the SurfaceHolder so the user can view it:
			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Start the preview!
			camera.startPreview();
		}
	}
	
	private void fixCamera() {
		Parameters parameters = camera.getParameters();
        List<Size> sizeList = parameters.getSupportedPreviewSizes();
        Size size = sizeList.get(3);
        parameters.setPreviewSize(size.width, size.height);
        parameters.setPreviewFpsRange(30000, 30000);
        camera.setParameters(parameters);
	}
	
	
	///// UNUSED /////
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}
