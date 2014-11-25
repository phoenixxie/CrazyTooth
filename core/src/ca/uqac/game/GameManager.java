package ca.uqac.game;

public final class GameManager {
	public static final int GOODPOINT = 2;
	public static final int GREATPOINT = 5;

	public static final int GOODSCORE = 200;
	public static final int GREATSCORE = 1000;

	private int score;
	private long startTime;
	private int level;
	private int points;

	public void reset() {
		score = 0;
		startTime = System.currentTimeMillis();
		level = 1;
		points = 0;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getPoints() {
		return points;
	}
	
	public int getDuration() {
		long duration = (System.currentTimeMillis() - startTime) / 1000;
		return (int)duration;
	}

	public void addScore(int score) {
		this.score += score;
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
