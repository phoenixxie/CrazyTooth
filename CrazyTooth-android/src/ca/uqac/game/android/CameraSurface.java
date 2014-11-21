package ca.uqac.game.android;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurface extends SurfaceView implements
		SurfaceHolder.Callback {
	private static final String TAG = "CameraSurface";
	private Camera camera;
	private MediaRecorder recorder;

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
		Size size = listSize.get(2);
		Log.v(TAG, "use: width = " + size.width + " height = " + size.height);
		p.setPreviewSize(size.width, size.height);
		p.setPreviewFormat(ImageFormat.NV21);
		camera.setParameters(p);
		// camera.setDisplayOrientation(90);
		try {
			camera.setPreviewDisplay(this.getHolder());
			camera.startPreview();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		camera.unlock();

		recorder = new MediaRecorder();
		recorder.setCamera(camera);
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
		recorder.setOutputFile(Environment.getExternalStorageDirectory()
				.getPath() + "/video.mp4");
		recorder.setVideoFrameRate(30);
		recorder.setVideoSize(size.width, size.height);
		recorder.setPreviewDisplay(this.getHolder().getSurface());
		try {
			recorder.prepare();
			recorder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void release() {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}

		if (recorder != null) {
			recorder.stop();
			recorder.reset();
			recorder.release();
		}
	}

}
