package com.sheetgame.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bob {
	
	public enum State {
		IDLE, WALKING, JUMPING, DYING
	}
	
	public static final float SIZE = 0.5f; //wielkosc bohatera
	
	Vector2		position = new Vector2();
	Vector2		acceleration = new Vector2();
	Vector2		velocity = new Vector2();
	Rectangle	bounds = new Rectangle();
	State		state = State.IDLE;
	boolean		facingLeft = true;
	float 		stateTime = 0;
	boolean 	longJump = false;
	
	
	public Bob(Vector2 pos) {
		this.position = pos;
		this.bounds.setX(pos.x);
		this.bounds.setY(pos.y);
		this.bounds.height = SIZE;
		this.bounds.width = SIZE;
	}
	
	public void update(float delta) {
		stateTime += delta;
	}
	
	public Vector2 getPosition() {
		return position;
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public void setFacingLeft(boolean set) {
		this.facingLeft = set;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public Vector2 getVelocity() {
		return velocity;
	}
	
	public Vector2 getAcceleration() {
		return acceleration;
	}

	public boolean isFacingLeft() {
		return facingLeft;
	}
	
	public State getState() {
		return state;
	}
	
	public float getStateTime() {
		return stateTime;
	}
	
	public boolean isLongJump() {
		return longJump;
	}
	
	public void setLongJump(Vector2 position) {
		this.position = position;
		this.bounds.setX(position.x);
		this.bounds.setY(position.y);
	}

	public void setPosition(Vector2 position2) {
		this.position = position2;
	}
}
