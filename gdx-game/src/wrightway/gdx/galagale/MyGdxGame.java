package wrightway.gdx.galagale;

import com.badlogic.gdx.*;

public class MyGdxGame extends Game{
	public static MyGdxGame game;
	
	@Override
	public void create(){
		game = this;
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		setScreen(new Galagale());
	}

	@Override
	public void dispose(){
		super.dispose();
		getScreen().dispose();
	}
}
