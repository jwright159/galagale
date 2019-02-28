package wrightway.gdx.galagale;

import wrightway.gdx.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.input.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

public class Galagale extends WScreen{
	public Galagale(){
		Log.setLogFile(Gdx.files.external("galagale.log"));
		Log.setVerbosity((byte)0b000_1111);

		Gdx.input.setCatchBackKey(true);
		getMultiplexer().addProcessor(0, new InputAdapter(){
				@Override
				public boolean keyDown(int keycode){
					if(keycode == Input.Keys.BACK){
						Gdx.app.exit();
						return true;
					}
					return false;
				}
			});
		/*getMultiplexer().addProcessor(2, new GestureDetector(new GestureDetector.GestureAdapter(){
		 @Override
		 public boolean tap(float x, float y, int count, int button){

		 return false;
		 }
		 }));*/

		loadObjs();
	}

	public WActor ship, bg;
	public Array<WActor> aliens;
	public TextureRegion shipTex, alienTex, lazorTex, bgTex;
	public Touchpad joystick;
	public static final float scale = 3, shootTimerMax = 0.5f, lazorSpeed = 10 * 96, lazorSpeedAlienFactor = 0.3f, spawnTimerMax = 3, alienSpeed = 300, panSensitivity = 2, cyanLimit = 1, splitWidth = 60, cyanFactor = 0.3f;
	public static final int splitCount = 20;
	public float spawnTimer, shipSpeed;
	public int score, highScore;
	public static Rectangle screen;
	public Label scoreLabel;
	public boolean paused = true;;
	public void loadObjs(){
		shipTex = new TextureRegion(new Texture(Gdx.files.internal("ship.png")));
		alienTex = new TextureRegion(new Texture(Gdx.files.internal("othership.png"))); // Some moron made this sprite upside down relative to the ship so the rotation math breaks
		lazorTex = new TextureRegion(new Texture(Gdx.files.internal("energy.png")));
		bgTex = new TextureRegion(new Texture(Gdx.files.internal("bg.jpg")));
		aliens = new Array<>();
		screen = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		getStage().addActor(bg = new WActor.WTexture(bgTex));
		if(screen.getWidth() > screen.getHeight())
			bg.setSize(screen.getWidth(), bgTex.getRegionHeight() * screen.getWidth() / bgTex.getRegionWidth());
		else
			bg.setSize(bgTex.getRegionWidth() * screen.getHeight() / bgTex.getRegionHeight(), screen.getHeight());
		bg.setPosition(screen.getWidth() / 2, screen.getHeight() / 2, Align.center);

		getStage().addActor(ship = new WActor.WTexture(shipTex){
				private float shootTimer = shootTimerMax, px, py;
				@Override
				public void act(float delta){
					shipSpeed = (float)Math.hypot(getX() - px, getY() - py);
					px = getX(); py = getY();
					if(paused)
						return;

					super.act(delta);
					shootTimer -= delta;
					if(shootTimer < 0){
						shootTimer = shootTimerMax;
						WActor lazor = new WActor.WTexture(lazorTex){
							@Override
							public void act(float delta){
								super.act(delta);
								if(!toRect().overlaps(screen))
									remove();
								for(int i = 0; i < aliens.size; i++)
									if(Math.hypot(aliens.get(i).getX(Align.center)-getX(Align.center), aliens.get(i).getY(Align.center)-getY(Align.center)) < getWidth()){
										remove();
										aliens.get(i).remove();
										aliens.removeIndex(i);
										scoreLabel.setText("Score: " + ++score + " - High Score: " + highScore);
									}
								moveBy((float)Math.cos((getRotation() + 90) * MathUtils.degreesToRadians) * lazorSpeed * delta, (float)Math.sin((getRotation() + 90) * MathUtils.degreesToRadians) * lazorSpeed * delta);
							}
						};
						getStage().addActor(lazor);
						lazor.scaleSizeBy(scale);
						lazor.setPosition(getX(Align.center), getY(Align.center), Align.center);
						lazor.setRotation(getRotation());
						lazor.setOrigin(Align.center);
					}
				}
			});
		ship.scaleSizeBy(scale);
		ship.setOrigin(Align.center);
		ship.setPosition(screen.getWidth() / 2, screen.getHeight() / 2, Align.center);


		Actor inputCatcher = new Actor(); // the global layer on the multiplexer doesn't like firing when the joystick is being used so this is my workaround
		getUiStage().addActor(inputCatcher);
		inputCatcher.setSize(screen.getWidth(), screen.getHeight());
		inputCatcher.addListener(new DragListener(){
				@Override
				public void drag(InputEvent event, float x, float y, int pointer){
					if(paused)
						return;
					ship.moveBy(getDeltaX() * panSensitivity, getDeltaY() * panSensitivity);
					ship.clamp(0, Gdx.graphics.getWidth(), 0, Gdx.graphics.getHeight());
				}

				@Override
				public void dragStart(InputEvent event, float x, float y, int pointer){
					if(paused){
						paused = false;
						for(WActor alien : aliens)
							alien.remove();
						aliens.clear();
						score = 0;
						scoreLabel.setText("Score: " + score + " - High Score: " + highScore);
						ship.setPosition(screen.getWidth() / 2, screen.getHeight() / 2, Align.center);
					}
				}
			});

		getUiStage().addActor(joystick = new Touchpad(5, getSkin()));
		joystick.setBounds(100, 100, 300, 300);
		joystick.setResetOnTouchUp(false);
		joystick.setColor(1, 1, 1, 0.6f);
		joystick.addListener(new ChangeListener(){
				@Override
				public void changed(ChangeEvent event, Actor actor){
					ship.setRotation((float)Math.atan(joystick.getKnobPercentY() / joystick.getKnobPercentX()) * MathUtils.radiansToDegrees + (joystick.getKnobPercentX() < 0 ? 180 : 0) - 90);
				}
			});

		getUiStage().addActor(scoreLabel = new Label("Welcome - touch to start", getSkin()));
		scoreLabel.setPosition(40, screen.getHeight() - 40, Align.topLeft);
	}

	@Override
	public void act(float delta){
		if(paused)
			return;

		spawnTimer -= delta;
		if(spawnTimer < 0){
			spawnTimer = spawnTimerMax/((float)Math.pow(score+1, 1/4f));
			WActor alien = new WActor.WTexture(alienTex){
				private float shootTimer = MathUtils.random(shootTimerMax);
				@Override
				public void act(float delta){
					setRotation((float)Math.atan((ship.getY() - getY()) / (ship.getX() - getX())) * MathUtils.radiansToDegrees + (ship.getX() < getX() ? 180 : 0) + 90);
					moveBy((float)Math.cos((getRotation() - 90) * MathUtils.degreesToRadians) * alienSpeed * delta, (float)Math.sin((getRotation() - 90) * MathUtils.degreesToRadians) * alienSpeed * delta);

					if(toRect().overlaps(ship.toRect())){
						if(score > highScore)
							highScore = score;
						scoreLabel.setText("Score: " + score + " - High Score: " + highScore + " - Game Over");
						paused = true;
					}

					shootTimer -= delta;
					if(shootTimer < 0){
						shootTimer = shootTimerMax / lazorSpeedAlienFactor;
						final boolean split = MathUtils.random() > 0.8f;
						for(int i = 0; i < (split ? splitCount : 1); i++){
							WActor lazor = new WActor.WTexture(lazorTex){
								@Override
								public void act(float delta){
									super.act(delta);
									if(!toRect().overlaps(screen))
										remove();
									if(Math.hypot(ship.getX(Align.center)-getX(Align.center), ship.getY(Align.center)-getY(Align.center)) < getWidth()){
										if(score > highScore)
											highScore = score;
										scoreLabel.setText("Score: " + score + " - High Score: " + highScore + " - Game Over");
										paused = true;
									}
									moveBy((float)Math.cos((getRotation() + 90) * MathUtils.degreesToRadians) * lazorSpeed * (split ? cyanFactor : 1) * delta, (float)Math.sin((getRotation() + 90) * MathUtils.degreesToRadians) * lazorSpeed * (split ? cyanFactor : 1) * delta);
								}
							};
							getStage().addActor(lazor);
							lazor.scaleSizeBy(scale);
							lazor.setPosition(getX(Align.center), getY(Align.center), Align.center);
							lazor.setRotation(getRotation() + 180 + (split ? -splitWidth / 2 + splitWidth / (splitCount - 1) * i : 0));
							lazor.setOrigin(Align.center);
						}
					}
				}
			};
			aliens.add(alien);
			getStage().addActor(alien);
			alien.scaleSizeBy(scale);
			alien.setOrigin(Align.center);
			boolean sides = MathUtils.randomBoolean(), other = MathUtils.randomBoolean();
			float x, y;
			if(sides){
				x = other ? -100 : screen.getWidth() + 100;
				y = MathUtils.random(0, screen.getHeight());
			}else{
				y = other ? -100 : screen.getHeight() + 100;
				x = MathUtils.random(0, screen.getWidth());
			}
			alien.setPosition(x, y);
		}
	}
}

/*
final int type = MathUtils.random(0, 7);
						int splits = type == 2 || type == 4 || type == 5 ? splitCount : 1;
						for(int i = 0; i < splits; i++){
							WActor lazor = new WActor.WTexture(lazorTex){
								@Override
								public void act(float delta){
									super.act(delta);
									if(!toRect().overlaps(screen))
										remove();
									if(Math.hypot(ship.getX(Align.center)-getX(Align.center), ship.getY(Align.center)-getY(Align.center)) < getWidth() && (type == 0 || type == 1 || (type == 2 && shipSpeed < cyanLimit) || type == 3 || (type == 5 && shipSpeed > cyanLimit))){
										if(score > highScore)
											highScore = score;
										scoreLabel.setText("Score: " + score + " - High Score: " + highScore + " - Game Over");
										paused = true;
									}
									moveBy((float)Math.cos((getRotation() + 90) * MathUtils.degreesToRadians) * lazorSpeed * (type == 2 || type == 4 || type == 5 ? cyanFactor : 1) * delta, (float)Math.sin((getRotation() + 90) * MathUtils.degreesToRadians) * lazorSpeed * (type == 2 || type == 4 || type == 5 ? cyanFactor : 1) * delta);
								}
							};
							switch(type){
								case 0:
									lazor.setColor(Color.WHITE); // generic tem flakes
									break;
								case 1:
									lazor.setColor(Color.RED); // more damage
									break;
								case 2:
									lazor.setColor(Color.ORANGE); // splits, ship must be moving
									break;
								case 3:
									lazor.setColor(Color.YELLOW); // shoots from itself
									break;
								case 4:
									lazor.setColor(Color.GREEN); // splits, stops ship
									break;
								case 5:
									lazor.setColor(Color.CYAN); // splits, ship must be still
									break;
								case 6:
									lazor.setColor(Color.BLUE); // carries ship to edge of screen
									break;
								case 7:
									lazor.setColor(Color.PURPLE); // limits ship to one axis
									break;
								default:
									throw new IllegalArgumentException("you wanna have a bad tom???");
							}
							getStage().addActor(lazor);
							lazor.scaleSizeBy(scale);
							lazor.setPosition(getX(Align.center), getY(Align.center), Align.center);
							lazor.setRotation(getRotation() + 180 + (splits > 1 ? -splitWidth / 2 + splitWidth / (splits - 1) * i : 0));
							lazor.setOrigin(Align.center);
						}
						*/
