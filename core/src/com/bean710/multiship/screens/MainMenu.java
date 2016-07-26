package com.bean710.multiship.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.bean710.multiship.MultiShip;

public class MainMenu implements Screen {

	OrthographicCamera camera;
	MultiShip game;

	public MainMenu(final MultiShip gam) {
		game = gam;
		
		camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, .7f, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		game.font.draw(game.batch, "Yolo", 100, 200, 100, 10, false);
		game.font.draw(game.batch, "Welcome to MultiShip!!! ", 100, 150);
		game.font.draw(game.batch, "Tap anywhere to begin!", 100, 100);
		game.batch.end();

		if (Gdx.input.isTouched()) {
			game.setScreen(new Game(game));
			dispose();
		}

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

}
