package ca.uqac.game;

public final class GameManager {
	public static final int GOODPOINT = 2;
	public static final int GREATPOINT = 5;

	public static final int GOODSCORE = 200;
	public static final int GREATSCORE = 1000;

	// SCORE, INTERVAL
	public static final int[][] LEVELS = { { 20000, 3 }, { 50000, 2 },
			{ 80000, 1 }, };

	private int score;
	private long startTime;
	private int level;
	private int points;
	private boolean paused;
	private long pausedTime;

	private CrazyTooth activity;

	public void reset() {
		score = 0;
		startTime = System.currentTimeMillis();
		pausedTime = 0;
		level = 0;
		points = 0;
		paused = false;
	}

	public void setActivity(CrazyTooth activity) {
		this.activity = activity;
	}

	public int getScore() {
		return score;
	}

	public int getPoints() {
		return points;
	}

	public int getInterval() {
		return LEVELS[level][1];
	}

	public int getDuration() {
		long pausedDuree = 0;
		if (paused) {
			pausedDuree = System.currentTimeMillis() - pausedTime;
		}
		long duration = (System.currentTimeMillis() - startTime - pausedDuree) / 1000;

		return (int) duration;
	}

	public void addScore(int score) {
		this.score += score;

		if (this.score >= LEVELS[level][0]) {
			if (level + 1 < LEVELS.length) {
				++level;
				activity.onUpgrade();
			} else {
				activity.onFinish();
			}
		}
	}

	public void addPoints(int points) {
		this.points += points;
	}

	public boolean usePoints(int points) {
		if (this.points < points) {
			return false;
		} else {
			this.points -= points;
			return true;
		}
	}

	public void pause() {
		if (paused) {
			return;
		}
		paused = true;
		pausedTime = System.currentTimeMillis();
	}

	public void resume() {
		if (!paused) {
			return;
		}
		paused = false;
		long pausedDuree = System.currentTimeMillis() - pausedTime;
		startTime += pausedDuree;
		pausedTime = 0;
	}

	private static GameManager instance;

	private GameManager() {
		reset();
	}

	public static GameManager instance() {
		if (instance == null) {
			instance = new GameManager();
		}

		return instance;
	}
}
