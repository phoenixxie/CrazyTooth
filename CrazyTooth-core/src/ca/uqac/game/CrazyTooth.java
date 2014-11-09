package ca.uqac.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CrazyTooth extends ApplicationAdapter {
	static final float SCREEN_WIDTH = 1280;
	static final float SCREEN_HEIGHT = 720;
	static final float ASPECT_RATIO = (float) SCREEN_WIDTH
			/ (float) SCREEN_HEIGHT;

	static final Rectangle LSTATUS = new Rectangle(0, 640, 200, 80);
	static final Rectangle RSTATUS = new Rectangle(1080, 640, 200, 80);

	static final float FACE_W = 300;
	static final float FACE_H = 350;

	static final Color COLOR_BACKGROUND = new Color(0.96875f, 0.9453125f,
			0.8515625f, 1);
	static final Color COLOR_LINE = new Color(0.77734375f, 0.68359375f,
			0.73828125f, 1);

	private Stage stage;
	private Viewport viewport;
	private Camera camera;
	private SpriteBatch batch;
	private ShapeRenderer renderer;
	private Skin skin;
	private BitmapFont font;
	private ImageButton buttonReset;

	private Texture faceImage;
	private float face_x;
	private float face_y;
	private float face_w;
	private float face_h;
	private float face_ratio;

	private Mouth mouth;

	@Override
	public void create() {

		camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
		camera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
		viewport = new ScalingViewport(Scaling.fit, SCREEN_WIDTH,
				SCREEN_HEIGHT, camera);
		stage = new Stage(viewport);

		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		font = new BitmapFont(Gdx.files.internal("data/fonts/fonts.fnt"),
				Gdx.files.internal("data/fonts/fonts.png"), false);

		TextureRegion btnResetImage = new TextureRegion(new Texture(Gdx.files.internal("data/refresh.png")));
		ImageButtonStyle style = new ImageButtonStyle();
		style.imageUp = new TextureRegionDrawable(btnResetImage);
		buttonReset = new ImageButton(style);
		
		buttonReset.setX(SCREEN_WIDTH - 20 - btnResetImage.getRegionWidth());
		buttonReset.setY(SCREEN_HEIGHT - 20 - btnResetImage.getRegionHeight());
		
		stage.addActor(buttonReset);
		
		faceImage = new Texture(Gdx.files.internal("images/face.png"));

		face_ratio = SCREEN_HEIGHT / FACE_H;
		face_h = SCREEN_HEIGHT;
		face_w = FACE_W * face_ratio;
		face_x = (SCREEN_WIDTH - face_w) / 2;
		face_y = 0;

		mouth = new Mouth(camera, face_x, face_y, face_ratio);

		Gdx.input.setInputProcessor(new InputMultiplexer(stage, mouth));
		
		buttonReset.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GameManager.instance().reset();
				mouth.reset();
			}

		});

	}

	@Override
	public void render() {
		super.render();

		Gdx.gl.glClearColor(COLOR_BACKGROUND.r, COLOR_BACKGROUND.g,
				COLOR_BACKGROUND.b, COLOR_BACKGROUND.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		mouth.checkTime();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(faceImage, face_x, face_y, face_w, face_h);
		mouth.draw(batch);
		drawStatus(batch);
		batch.end();

		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeType.Line);
		renderer.setColor(COLOR_LINE);
		// renderer.rect(LSTATUS.x, LSTATUS.y, LSTATUS.width, LSTATUS.height);
		// renderer.rect(RSTATUS.x, RSTATUS.y, RSTATUS.width, RSTATUS.height);

		renderer.end();
		
		stage.draw();
	}

	private void drawStatus(Batch batch) {
		long duration = GameManager.instance().getDuration();
		int seconds = (int) (duration % 60);
		duration /= 60;
		int minutes = (int) (duration % 60);
		int hours = (int) (duration / 60);

		font.setColor(0f, 0f, 0f, 1f);
		font.draw(batch, "Time:   " + hours + ":"
				+ (minutes < 10 ? "0" + minutes : minutes) + ":"
				+ (seconds < 10 ? "0" + seconds : seconds), 30,
				SCREEN_HEIGHT - 48 - 10);
		font.draw(batch, "Points:  " + GameManager.instance().getPoints(), 30,
				SCREEN_HEIGHT - 48 - 58);
		font.draw(batch, "Score:  " + GameManager.instance().getScore(), 30,
				SCREEN_HEIGHT - 48 - 106);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
}
