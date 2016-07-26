package com.bean710.multiship;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Bullet extends Sprite {
	private int speed;

	public Bullet(Texture texture, int speed) {
		super(texture);
		this.speed = speed;
	}

	public float getSpeed() {
		return speed;
	}
}
