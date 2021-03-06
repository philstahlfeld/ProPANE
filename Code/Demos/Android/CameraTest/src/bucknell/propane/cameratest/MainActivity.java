package bucknell.propane.cameratest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import phil.stahlfeld.propane.R;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final int MEDIA_TYPE_VIDEO = 2;

	protected static final String TAG = "ProPANE ";

	private Camera mCamera;
	private Handler mHandler;
	private Messenger captureMessenger;
	private Handler messageHandler = new Handler() {
		public void handleMessage(Message message) {
			Object path = message.obj;
			if (message.arg1 == RESULT_OK && path != null) {
				Toast.makeText(MainActivity.this,
						"Downloaded" + path.toString(), Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(MainActivity.this, "Download failed.",
						Toast.LENGTH_LONG).show();
			}

		};
	};

	private boolean capturing;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mHandler = new Handler();

		mCamera = getCameraInstance();

		CameraPreview mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

		preview.addView(mPreview);

		findViewById(R.id.stop_capture).setEnabled(false);
		capturing = false;
	}

	public void stopCapture(View view) {
		findViewById(R.id.start_capture).setEnabled(true);
		findViewById(R.id.stop_capture).setEnabled(false);
		capturing = false;

	}

	public void startCapture(View view) throws InterruptedException {
		// Signal to start capturing
		findViewById(R.id.start_capture).setEnabled(false);
		findViewById(R.id.stop_capture).setEnabled(true);
		capturing = true;

		Intent captureIntent = new Intent(this, CaptureService.class);
		captureMessenger = new Messenger(messageHandler);
		captureIntent.putExtra("MESSENGER", captureMessenger);
		captureIntent.putExtra("CAMERA", );
		startService(captureIntent);

		// When autofocus completes, take picture
		// AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback()
		// {
		//
		// public void onAutoFocus(boolean success, Camera camera) {
		// mCamera.takePicture(null, null, mPicture);
		// Log.d(TAG, "TAKE PICTURE WAS CALLED");
		// }
		// };
		//
		// // Start autofocusing
		// mCamera.autoFocus(mAutoFocusCallback);

	}

	private PictureCallback mPicture = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.d(TAG,
						"Error creating media file, check storage permissions: ");
				return;
			}

			try {
				// Start the preview again
				mCamera.startPreview();

				// Write the picture to storage
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();

				// If still capturing, wait then take another picture
				if (capturing) {
					mHandler.postDelayed(new Runnable() {
						public void run() {
							mCamera.takePicture(null, null, mPicture);
						}
					}, 5000);
				}

			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
		}
	};

	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"ProPANE");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("ProPANE", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {

		}
		return c;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
