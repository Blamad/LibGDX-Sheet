package com.sheetgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.sheetgame.controller.BobController;
import com.sheetgame.model.World;
import com.sheetgame.view.WorldRenderer;

public class GameScreen implements Screen, InputProcessor {

	private World world;
	private WorldRenderer renderer;
	private BobController controller;
	
	private int width, height;

	
	/* TO JEST KONSTRUKTOR! */
	@Override
	public void show() {
		world = new World();
		renderer = new WorldRenderer(world, true); //debugowanie!
		controller = new BobController(world);
		Gdx.input.setInputProcessor(this);
		
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		controller.update(delta);
		renderer.render();

	}

	@Override
	public void resize(int width, int height) {
		renderer.setSize(width, height);
		this.width = width;
		this.height = height;
	}

	@Override
	public void hide() {
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
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/*	 Metody wejscia procesora 	*/
	
	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Keys.LEFT)
			controller.leftPressed();
		if(keycode == Keys.RIGHT)
			controller.rightPressed();
		if(keycode == Keys.Z)
			controller.jumpPressed();
		if(keycode == Keys.X)
			controller.leftPressed();
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Keys.LEFT)
			controller.leftReleased();
		if(keycode == Keys.RIGHT)
			controller.rightReleased();
		if(keycode == Keys.Z)
			controller.jumpReleased();
		if(keycode == Keys.X)
			controller.leftReleased();
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		if(x < width / 2 && y > height / 2) {
			controller.leftPressed();
		}
		if(x > width / 2 && y > height / 2) {
			controller.rightPressed();
		}
		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if(x < width / 2 && y > height / 2) {
			controller.leftReleased();
		}
		if(x > width / 2 && y > height / 2) {
			controller.rightReleased();
		}
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
