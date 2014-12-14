package ca.uqac.game.android;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurface extends SurfaceView implements
		SurfaceHolder.Callback {
	private static final String TAG = "CameraSurface";
	private Camera camera;
	private Size cameraSize;

	static boolean running = true;

	Thread recordThread;

	public CameraSurface(Context context) {
		super(context);
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		initCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Camera.Parameters p = camera.getParameters();
		// p.setPreviewSize(width, height);
		// camera.setParameters(p);
		//
		// try {
		// camera.setPreviewDisplay(holder);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		release();
	}

	private void initCamera() {
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					camera = Camera.open(camIdx);
//					camera.setDisplayOrientation(90);

				} catch (RuntimeException e) {
					e.printStackTrace();
					Log.e(TAG,
							"Camera failed to open: " + e.getLocalizedMessage());
					return;
				}
			}
		}

		if (camera == null) {
			return;
		}

		Camera.Parameters p = camera.getParameters();

		final List<Size> listSize = p.getSupportedPreviewSizes();
		cameraSize = listSize.get(2);
		Log.v(TAG, "use: width = " + cameraSize.width + " height = "
				+ cameraSize.height);
		p.setPreviewSize(cameraSize.width, cameraSize.height);
		p.setPreviewFormat(ImageFormat.NV21);
		p.set("cam_mode", 1);
//		p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		camera.setParameters(p);

		try {
			camera.setPreviewDisplay(this.getHolder());
			camera.startPreview();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		camera.unlock();

		recordThread = new RecordThread();
		recordThread.start();
	}

	private void release() {
		running = false;
		if (recordThread != null) {
			try {
				recordThread.join();
			} catch (InterruptedException e) {
			}
		}

		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	class RecordThread extends Thread {

		@Override
		public void run() {
			String uuid = UUID.randomUUID().toString();

			UploadService service = new UploadService(getContext(), uuid);
			service.start();

			MediaRecorder recorder = null;

			while (running) {
				String filename = getContext().getExternalFilesDir(null)
						.getPath()
						+ "/"
						+ uuid
						+ "."
						+ System.currentTimeMillis() + ".mp4";

				recorder = new MediaRecorder();
				recorder.setCamera(camera);
				recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
				recorder.setOutputFile(filename);
				recorder.setVideoFrameRate(30);

				recorder.setVideoSize(cameraSize.width, cameraSize.height);
				recorder.setPreviewDisplay(CameraSurface.this.getHolder()
						.getSurface());
				recorder.setOrientationHint(270);

				try {
					recorder.prepare();
					recorder.start();

				} catch (Exception e) {
					recorder = null;
					e.printStackTrace();
				}

				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (recorder != null) {
					recorder.stop();
					recorder.reset();
					recorder.release();
					recorder = null;

					service.addFile(filename);
					camera.unlock();
				}
			}
		}
	}

}
