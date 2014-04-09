package com.sheetgame;

import com.badlogic.gdx.Game;
import com.sheetgame.screens.GameScreen;

public class SheetGame extends Game {


	
	@Override
	public void create() {
		setScreen(new GameScreen());
	}
	
}