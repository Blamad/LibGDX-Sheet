package com.sheetgame.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.sheetgame.model.Block;
import com.sheetgame.model.Bob;
import com.sheetgame.model.Bob.State;
import com.sheetgame.model.World;

public class WorldRenderer {

	private static final float CAMERA_WIDTH = 10f;
	private static final float CAMERA_HEIGHT = 7f;
	
	private static final float RUNNING_FRAME_DURATION = 0.06f;
	
	private World world;
	private OrthographicCamera cam;
	
	/** Debug **/
	ShapeRenderer debugRenderer = new ShapeRenderer();
	
	/** Texturki **/
	private TextureRegion bobIdleLeft;
	private TextureRegion bobIdleRight;
	private TextureRegion blockTexture;
	private TextureRegion bobFrame;
	private TextureRegion bobJumpLeft;
	private TextureRegion bobFallLeft;
	private TextureRegion bobJumpRight;
	private TextureRegion bobFallRight;
	
	
	/** Animations **/ 
	private Animation walkLeftAnimation;
	private Animation walkRightAnimation;
	
	
	private SpriteBatch spriteBatch;
	private boolean debug = false;
	private int width;
	private int height;
	private float ppuX; // pixel per unit X
	private float ppuY; // pixel per unit Y
	
	/* to ppu to fajna sprawa, w ten sposob na kazdym
	 * urzadzeniu bedziesz mial te sama ilosc bloczkow (ile dl. ma 1 bloczek)
	 */
	
	public void setSize (int w, int h) {
		this.width = w;
		this.height = h;
		ppuX = (float)width / CAMERA_WIDTH;
		ppuY = (float)height / CAMERA_HEIGHT;
	}
	
	
	public WorldRenderer(World world, boolean debug) {
		this.world = world;
		this.cam = new OrthographicCamera(CAMERA_WIDTH,CAMERA_HEIGHT);
		this.cam.position.set(5, 3.5f, 0);
		this.cam.update();
		this.debug = debug;
		spriteBatch = new SpriteBatch();
		loadTextures();
	}
	
	
	/** uwaga
	 * wazne zebys przy odbijaniu textur na druga strone korzystal z lewej
	 * inaczej odbicie nie dziala (czemu?)
	 */
	private void loadTextures() {
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("images/sheetgame.pack"));
		bobIdleLeft = atlas.findRegion("bob-0");
		
		bobIdleRight = new TextureRegion(bobIdleLeft);
		bobIdleRight.flip(true,false);
		
		blockTexture = atlas.findRegion("block");
		TextureRegion[] walkLeftFrames = new TextureRegion[5];
		for(int i = 0; i < 5; i++) {
			walkLeftFrames[i] = atlas.findRegion("bob-" + (i + 1));
		}
		walkLeftAnimation = new Animation(RUNNING_FRAME_DURATION, walkLeftFrames);
		
		TextureRegion[] walkRightFrames = new TextureRegion[5];
		for(int i = 0; i < 5; i++) {
			walkRightFrames[i] = new TextureRegion(walkLeftFrames[i]);
			walkRightFrames[i].flip(true, false);
		}
		walkRightAnimation = new Animation(RUNNING_FRAME_DURATION, walkRightFrames);
	
		bobJumpLeft = atlas.findRegion("bob-jump");
		bobJumpRight = new TextureRegion(bobJumpLeft);
		bobJumpRight.flip(true,false);
		bobFallLeft = atlas.findRegion("bob-fall");
		bobFallRight = new TextureRegion(bobFallLeft);
		bobFallRight.flip(true,false);
	
	}
	
	public void render() {
		spriteBatch.begin();
			drawBlocks();
			drawBob();
		spriteBatch.end();
		drawCollisionBlocks();
		if(debug)
			drawDebug();
	}
	
	private void drawBlocks() {
		for (Block block : world.getDrawableBlocks((int) CAMERA_WIDTH, (int) CAMERA_HEIGHT)) {
			spriteBatch.draw(blockTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, block.SIZE * ppuX, block.SIZE * ppuY);
		}
	}
	private void drawBob() {
		Bob bob = world.getBob();
		bobFrame = bob.isFacingLeft() ? bobIdleLeft : bobIdleRight;
		if(bob.getState().equals(State.WALKING)) {
			bobFrame = bob.isFacingLeft() ? walkLeftAnimation.getKeyFrame(bob.getStateTime(), true) : walkRightAnimation.getKeyFrame(bob.getStateTime(), true);
		} else if(bob.getState().equals(State.JUMPING)) {
			if(bob.getVelocity().y > 0) {
				bobFrame = bob.isFacingLeft() ? bobJumpLeft : bobJumpRight;
			} else {
				bobFrame = bob.isFacingLeft() ? bobFallLeft : bobFallRight;
			}
		}
		spriteBatch.draw(bobFrame, bob.getPosition().x * ppuX, bob.getPosition().y * ppuY, bob.SIZE * ppuX, bob.SIZE * ppuY);
	}
	
	public void drawDebug() {
		//renderuj bloczki
		debugRenderer.setProjectionMatrix(cam.combined);
		debugRenderer.begin(ShapeType.Rectangle); //wazne!! Taka formula
		
		for(Block block : world.getDrawableBlocks((int) CAMERA_WIDTH, (int)CAMERA_HEIGHT)) {
			Rectangle rect = block.getBounds();
			float x1 = block.getPosition().x;
			float y1 = block.getPosition().y;
			debugRenderer.setColor(new Color(1,0,0,1));
			debugRenderer.rect(x1, y1, rect.width, rect.height);
		}
		//render bob
		Bob bob = world.getBob();
		Rectangle rect = bob.getBounds();
		float x1 = bob.getPosition().x;
		float y1 = bob.getPosition().y;
		debugRenderer.setColor(new Color(0,1,0,1));
		debugRenderer.rect(x1, y1, rect.width, rect.height);
		debugRenderer.end();
	}
	
	private void drawCollisionBlocks() {
		debugRenderer.setProjectionMatrix(cam.combined);
		debugRenderer.begin(ShapeType.FilledRectangle);
		debugRenderer.setColor(new Color(1,1,1,1));
		for(Rectangle rect : world.getCollisionRects()) {
			debugRenderer.filledRect(rect.x,rect.y, rect.width, rect.height);
		}
		debugRenderer.end();
		
	}
	
}
