package ca.uqac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class Tooth {
	public static final int SCORES[] = { 0, 1000, 200, 0, 0 };
	public static final int TIMES[] = { 0, 1, 2, 1, Integer.MAX_VALUE };
	public static final int POINTS[] = { 0, 5, 2, -100, 0 };

	public static final long INTERVAL_CHANGE = 4; // secondes

	public static final int STATE_PERFECT = 0;
	public static final int STATE_SLIGHT = 1;
	public static final int STATE_TERRIBLE = 2;
	public static final int STATE_DEAD = 3;
	public static final int STATE_LOST = 4;
	
	private static final Music soundLostTooth = Gdx.audio.newMusic(Gdx.files
			.internal("sound/cartoon-male-crying.wav"));

	private int state;
	private long lastStateTime;
	private int currTimes;
	private Rectangle position;
	private Texture[] textures;

	public Tooth(float offx, float offy, float ratio, Rectangle position,
			Texture[] textures) {
		this.position = position;
		this.textures = textures;

		this.position.x = offx + this.position.x * ratio;
		this.position.y = offy + this.position.y * ratio;
		this.position.width = this.position.width * ratio;
		this.position.height = this.position.height * ratio;

		reset();
	}

	public void reset() {
		this.state = STATE_PERFECT;
		this.currTimes = TIMES[0];
		this.lastStateTime = System.currentTimeMillis();
	}

	public boolean checkTime() {
		if (state == STATE_LOST) { // pas besoin de check
			return false;
		}

		long now = System.currentTimeMillis();
		long diff = (now - lastStateTime) / 1000;

		if (diff < INTERVAL_CHANGE) {
			return false;
		}

		++state;
		lastStateTime = now;
		
		if (state == STATE_LOST) {
			soundLostTooth.play();
		}
		return true;
	}

	public boolean brush() {
		if (state == STATE_LOST) { // ça ne marche plus
			return false;
		}

		int times = currTimes - 1;
		int points = GameManager.instance().getPoints() + POINTS[state];

		if (times > 0) { // pas suffisant
			currTimes = times;
			return false;
		}

		if (points < 0) { // point n'est pas suffisant
			return false;
		}

		lastStateTime = System.currentTimeMillis();
		if (state == STATE_PERFECT) {
			return false;
		}

		GameManager.instance().addPoints(POINTS[state]);
		GameManager.instance().addScore(SCORES[state]);
		--state;
		currTimes = TIMES[state];

		return true;
	}

	public int getState() {
		return this.state;
	}

	public void draw(Batch batch) {
		if (this.state == STATE_LOST) {
			return;
		}

		batch.draw(this.textures[this.state], position.x, position.y,
				position.width, position.height);
	}

	public final Rectangle getPosition() {
		return position;
	}
}
