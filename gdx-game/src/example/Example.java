package example;

import wrightway.gdx.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.input.*;

public class Example extends WScreen{
	public Example(){
		Log.setLogFile(Gdx.files.external());
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
		getMultiplexer().addProcessor(2, new GestureDetector(new GestureDetector.GestureAdapter(){
			@Override
			public boolean pan(float x, float y, float dx, float dy){
				y *= -1; dy *= -1;
				return false;
			}
		}));
	}
}
