package com.sheetgame.controller;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.sheetgame.model.Block;
import com.sheetgame.model.Bob;
import com.sheetgame.model.Bob.State;
import com.sheetgame.model.World;

public class BobController {

	enum Keys {
		LEFT, RIGHT, JUMP, FIRE
	}
	
	private static final long LONG_JUMP_PRESS 	= 150l;
	private static final float ACCELERATION 	= 20f;
	private static final float GRAVITY			= -20f;
	private static final float MAX_JUMP_SPEED	= 7f;
	private static final float DAMP				= 0.9f;
	private static final float MAX_VEL			= 4f;
	
	private World world;
	private Bob bob;
	private long jumpPressedTime;
	private boolean jumpingPressed;
	private boolean grounded = false;
	
	private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};
	
	private Array<Block> collidable = new Array<Block>();
	/* ciekawa rzecz, ogarnij to */
	static Map<Keys, Boolean> keys = new HashMap<BobController.Keys, Boolean>();
	static {
		keys.put(Keys.LEFT, false);
		keys.put(Keys.RIGHT, false);
		keys.put(Keys.JUMP, false);
		keys.put(Keys.FIRE, false);
	}
	
	public BobController(World world) {
		this.world = world;
		this.bob = world.getBob();
	}
		
	public void update(float delta) {
		//ustaw stan boba
		processInput();
		//jezeli bob spadl to ustaw stan na IDLE
		if(grounded && bob.getState().equals(State.JUMPING)){
			bob.setState(State.IDLE);
		}
		//ustawienie poczatkowego y przyspieszenia
		bob.getAcceleration().y = GRAVITY;
		//przemien to na frametime
		bob.getAcceleration().mul(delta);
		//uaktualnij zmiany
		bob.getVelocity().add(bob.getAcceleration().x, bob.getAcceleration().y);	//predkosc boba = wektor przysp. x i y.
		//sprawdz kolizje
		checkCollisionWithBlocks(delta);
		//zatrzymaj boba jezeli jest potrzeba
		if(bob.getAcceleration().x == 0) bob.getVelocity().x *=DAMP; 				//jezeli przysp. = 0, hamuj
		if(bob.getVelocity().x > MAX_VEL) {											//jezeli predkosc > max, trzymaj sie maxa
			bob.getVelocity().x = MAX_VEL;
		}
		if(bob.getVelocity().x < -MAX_VEL) {
			bob.getVelocity().x = -MAX_VEL;
		}
		bob.update(delta);
	}
	
	// rectangle pool, przydatny do detekcji kolizji(!)
	// optymalniejszy myk od budowania prostokatow za kazdym razem
	private void checkCollisionWithBlocks(float delta) {
		//przeskaluj predkosc na parametry klatki
		bob.getVelocity().mul(delta);
		
		//wez rectangle z poola zamiast go inicjowac
		Rectangle bobRect = rectPool.obtain();
		//ustaw go do boba
		bobRect.set(bob.getBounds().x, bob.getBounds().y, bob.getBounds().width, bob.getBounds().height);
		
		//bloki do sprawdzenia to sasiadujace w kierunku ruchu x boba oraz w kier. ruchu y.
		//sprawdzmy ruch
		int startX, endX;
		int startY = (int)bob.getBounds().y;
		int endY = (int)(bob.getBounds().y + bob.getBounds().height);
		//jezeli bob zmierza w lewo, sprawdzamy lewy, jezeli nie, prawy
		if(bob.getVelocity().x < 0) {
			startX = endX = (int) Math.floor(bob.getBounds().x + bob.getVelocity().x);
		} else {
			startX = endX = (int) Math.floor(bob.getBounds().x + bob.getBounds().width + bob.getVelocity().x);
		}
		// zdobadz bloczki mogace kolidowac z bobem
		populateCollidableBlocks(startX, startY, endX, endY);
		
		// symulacja ruchu boba w kier. x
		bobRect.x += bob.getVelocity().x;
		// czysc wektor kolizyjny
		world.getCollisionRects().clear();
		// jezeli bob koliduje, jego pr. = 0
		for(Block block : collidable) {
			if(block == null) continue;
			if(bobRect.overlaps(block.getBounds())) {
				bob.getVelocity().x = 0;
				world.getCollisionRects().add(block.getBounds());
				break;
			}
		}
		//restetuj x pozycje recta boba
		bobRect.x = bob.getPosition().x;
		
		//to samo ale dla osi y
		startX = (int)bob.getBounds().x;
		endX = (int)(bob.getBounds().x + bob.getBounds().width);
		if(bob.getVelocity().y < 0) {
			startY = endY = (int) Math.floor(bob.getBounds().y + bob.getVelocity().y);
		} else {
			startY = endY = (int) Math.floor(bob.getBounds().y + bob.getVelocity().y + bob.getBounds().height);
		}
		
		populateCollidableBlocks(startX, startY, endX, endY);
		
		bobRect.y += bob.getVelocity().y;
		
		for(Block block : collidable) {
			if(block == null) continue;
			if(bobRect.overlaps(block.getBounds())) {
				if(bob.getVelocity().y < 0) {
					grounded = true;
				}
				bob.getVelocity().y = 0;
				world.getCollisionRects().add(block.getBounds());
				break;
			}
		}
		//resetuj box kolizyjny dla y
		bobRect.y = bob.getPosition().y;
		
		//update pozycji boba
		bob.getPosition().add(bob.getVelocity());
		bob.getBounds().x = bob.getPosition().x;
		bob.getBounds().y = bob.getPosition().y;
		
		//odskaluj predkosc
		bob.getVelocity().mul(1/delta);
	}
	
	/** zalej collidable array kandydatami pod koordynatami **/
	private void populateCollidableBlocks(int startX, int startY, int endX, int endY){
		collidable.clear();
		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				if( x >= 0 && x < world.getLevel().getWidth() && y >= 0 && y < world.getLevel().getHeight()) {
					collidable.add(world.getLevel().get(x,y));
				}
			}
		}
	}
	
	/* przyciski */
	
	public void leftPressed() {
		keys.get(keys.put(Keys.LEFT,true));
	}
	
	public void rightPressed() {
		keys.get(keys.put(Keys.RIGHT,true));
	}
	
	public void jumpPressed() {
		keys.get(keys.put(Keys.JUMP,true));
	}
	
	public void firePressed() {
		keys.get(keys.put(Keys.FIRE,true));
	}
	
	public void leftReleased() {
		keys.get(keys.put(Keys.LEFT,false));
		jumpingPressed = false;
	}
	
	public void rightReleased() {
		keys.get(keys.put(Keys.RIGHT,false));
	}
	
	public void jumpReleased() {
		keys.get(keys.put(Keys.JUMP,false));
		jumpingPressed = false;
	}
	
	public void fireReleased() {
		keys.get(keys.put(Keys.FIRE,false));
	}
	
	private boolean processInput() {
		if(keys.get(Keys.JUMP)) {
			if(!bob.getState().equals(State.JUMPING)) {
				jumpingPressed = true;
				jumpPressedTime = System.currentTimeMillis();
				bob.setState(State.JUMPING);
				bob.getVelocity().y = MAX_JUMP_SPEED;
				grounded = false;
			} else {
				if(jumpingPressed && ((System.currentTimeMillis() - jumpPressedTime) >= LONG_JUMP_PRESS)) {
					jumpingPressed = false;
				} else {
					if(jumpingPressed) {
						bob.getVelocity().y = MAX_JUMP_SPEED;
					}
				}
			}
		}
		
		if(keys.get(Keys.LEFT)) {
			//lewo
			bob.setFacingLeft(true);
			if(!bob.getState().equals(State.JUMPING)) {
				bob.setState(State.WALKING);
			}
			bob.getAcceleration().x = -ACCELERATION;
		}
		else if(keys.get(Keys.RIGHT)) {
			bob.setFacingLeft(false);
			if(!bob.getState().equals(State.JUMPING)) {
				bob.setState(State.WALKING);
			}
			bob.getAcceleration().x = ACCELERATION;
		} else {
			if(!bob.getState().equals(State.JUMPING)) {
				bob.setState(State.IDLE);
			}	
			bob.getAcceleration().x = 0;
		}
		return false;
	}
}
