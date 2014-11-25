package ca.uqac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Mouth extends InputAdapter {
	public static final int TOOTH_COUNT = 10;
	public static final Rectangle[] TEETH_POSITIONS = new Rectangle[] {
			// haut
			new Rectangle(85f, 430f, 140f, 140f),
			new Rectangle(180f, 390f, 140f, 140f),
			new Rectangle(275f, 375f, 140f, 140f),
			new Rectangle(370f, 390f, 140f, 140f),
			new Rectangle(465f, 430f, 140f, 140f),

			// bas
			new Rectangle(75f, 300f, 140f, 140f),
			new Rectangle(175f, 275f, 140f, 140f),
			new Rectangle(275f, 273f, 140f, 140f),
			new Rectangle(375f, 275f, 140f, 140f),
			new Rectangle(475f, 300f, 140f, 140f), };

	public static final Texture[][] TEETH_IMAGES = {
			// haut
			new Texture[] { new Texture("images/haut/1/blanc.png"),
					new Texture("images/haut/1/peu-sale.png"),
					new Texture("images/haut/1/sale.png"),
					new Texture("images/haut/1/tres-sale.png"), },
			new Texture[] { new Texture("images/haut/2/blanc.png"),
					new Texture("images/haut/2/peu-sale.png"),
					new Texture("images/haut/2/sale.png"),
					new Texture("images/haut/2/tres-sale.png"), },
			new Texture[] { new Texture("images/haut/3/blanc.png"),
					new Texture("images/haut/3/peu-sale.png"),
					new Texture("images/haut/3/sale.png"),
					new Texture("images/haut/3/tres-sale.png"), },
			new Texture[] { new Texture("images/haut/4/blanc.png"),
					new Texture("images/haut/4/peu-sale.png"),
					new Texture("images/haut/4/sale.png"),
					new Texture("images/haut/4/tres-sale.png"), },
			new Texture[] { new Texture("images/haut/5/blanc.png"),
					new Texture("images/haut/5/peu-sale.png"),
					new Texture("images/haut/5/sale.png"),
					new Texture("images/haut/5/tres-sale.png"), },
			// bas
			new Texture[] { new Texture("images/bas/1/blanc.png"),
					new Texture("images/bas/1/peu-sale.png"),
					new Texture("images/bas/1/sale.png"),
					new Texture("images/bas/1/tres-sale.png"), },
			new Texture[] { new Texture("images/bas/2/blanc.png"),
					new Texture("images/bas/2/peu-sale.png"),
					new Texture("images/bas/2/sale.png"),
					new Texture("images/bas/2/tres-sale.png"), },
			new Texture[] { new Texture("images/bas/3/blanc.png"),
					new Texture("images/bas/3/peu-sale.png"),
					new Texture("images/bas/3/sale.png"),
					new Texture("images/bas/3/tres-sale.png"), },
			new Texture[] { new Texture("images/bas/4/blanc.png"),
					new Texture("images/bas/4/peu-sale.png"),
					new Texture("images/bas/4/sale.png"),
					new Texture("images/bas/4/tres-sale.png"), },
			new Texture[] { new Texture("images/bas/5/blanc.png"),
					new Texture("images/bas/5/peu-sale.png"),
					new Texture("images/bas/5/sale.png"),
					new Texture("images/bas/5/tres-sale.png"), }, };

	public static final float EFFECT_WIDTH = 30;
	public static final float EFFECT_HEIGHT = 30;

	private static final Music soundBrush = Gdx.audio.newMusic(Gdx.files
			.internal("sound/brushing-teeth.wav"));

	private Tooth[] teeth;
	private Rectangle range;
	private Camera camera;

	private Vector2 lastTouch;
	private boolean isSwiping;

	private Texture brushImage;
	private float brushW;
	private float brushH;

	public Mouth(Camera camera, float offx, float offy, float ratio) {
		this.camera = camera;

		teeth = new Tooth[TOOTH_COUNT];

		float x1 = CrazyTooth.SCREEN_WIDTH, y1 = CrazyTooth.SCREEN_HEIGHT;
		float x2 = 0, y2 = 0;
		Rectangle pos;
		for (int i = 0; i < TOOTH_COUNT; ++i) {
			teeth[i] = new Tooth(offx, offy, ratio, TEETH_POSITIONS[i],
					TEETH_IMAGES[i]);

			pos = teeth[i].getPosition();
			if (pos.x < x1) {
				x1 = pos.x;
			}
			if (pos.x + pos.width > x2) {
				x2 = pos.x + pos.width;
			}
			if (pos.y < y1) {
				y1 = pos.y;
			}
			if (pos.y + pos.height > y2) {
				y2 = pos.y + pos.width;
			}
		}
		x1 -= 200;
		y1 -= 200;
		x2 += 200;
		y2 += 200;
		if (x1 < 0) {
			x1 = 0;
		}
		if (y1 < 0) {
			y1 = 0;
		}

		range = new Rectangle(x1, y1, x2 - x1, y2 - y1);

		lastTouch = new Vector2(0, 0);

		brushImage = new Texture("images/bas/3/peu-sale.png");
		brushW = 40;
		brushH = 40;

		reset();
	}

	public boolean checkTime() {
		int left = 0;
		for (int i = 0; i < TOOTH_COUNT; ++i) {
			teeth[i].checkTime();
			if (teeth[i].getState() != Tooth.STATE_LOST) {
				++left;
			}
		}
		
		if (left == 0) {
			return true;
		} else {
			return false;
		}
	}

	public void reset() {
		for (int i = 0; i < TOOTH_COUNT; ++i) {
			teeth[i].reset();
		}

		isSwiping = false;
	}

	public void draw(Batch batch) {
		for (int i = 0; i < TOOTH_COUNT; ++i) {
			teeth[i].draw(batch);
		}

		if (isSwiping) {
			batch.draw(brushImage, lastTouch.x - brushW / 2, lastTouch.y
					- brushH / 2, brushW, brushH);
		}
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		soundBrush.play();
		
		Vector3 vec = new Vector3(screenX, screenY, 0);
		camera.unproject(vec);

		if (vec.x < range.x || vec.y < range.y || vec.x > range.x + range.width
				|| vec.y > range.y + range.height) {
			isSwiping = true;
			return false;
		}

		if (isSwiping) {
			for (int i = 0; i < TOOTH_COUNT; ++i) {
				Rectangle rect = teeth[i].getPosition();
				float x1, y1;
				float x2, y2;

				x1 = rect.x + (rect.width - EFFECT_WIDTH) / 2;
				y1 = rect.y + (rect.height - EFFECT_HEIGHT) / 2;
				x2 = x1 + EFFECT_WIDTH;
				y2 = y1 + EFFECT_HEIGHT;

				if (Intersector.intersectSegments(vec.x, vec.y, lastTouch.x,
						lastTouch.y, x1, y1, x2, y1, null)
						|| Intersector.intersectSegments(vec.x, vec.y,
								lastTouch.x, lastTouch.y, x1, y1, x1, y2, null)
						|| Intersector.intersectSegments(vec.x, vec.y,
								lastTouch.x, lastTouch.y, x1, y2, x2, y2, null)
						|| Intersector.intersectSegments(vec.x, vec.y,
								lastTouch.x, lastTouch.y, x2, y1, x2, y2, null)) {
					// System.out.println(i + " " + vec.x + "," + vec.y + " " +
					// lastTouch.x
					// + "," + lastTouch.y + " " + x1 + "," + y1 + " " + x2 +
					// "," + y2);
					teeth[i].brush();
				}
			}
		}

		isSwiping = true;
		lastTouch.x = vec.x;
		lastTouch.y = vec.y;

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		isSwiping = false;
		return false;
	}
}
