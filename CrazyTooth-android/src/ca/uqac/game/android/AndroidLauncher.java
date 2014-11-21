package ca.uqac.game.android;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import ca.uqac.game.CrazyTooth;

public class AndroidLauncher extends AndroidApplication {
	CameraSurface cameraSurface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new CrazyTooth(), config);

		cameraSurface = new CameraSurface(this);
		this.addContentView(cameraSurface, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
