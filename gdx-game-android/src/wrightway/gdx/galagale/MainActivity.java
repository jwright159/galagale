package wrightway.gdx.galagale;

import android.content.pm.*;
import android.os.*;
import com.badlogic.gdx.backends.android.*;
import java.io.*;
import java.lang.Process;
import android.util.*;
import android.app.*;
import android.content.*;
import java.util.*;
import com.badlogic.gdx.*;
import wrightway.gdx.Log;

public class MainActivity extends AndroidApplication{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

		final Thread.UncaughtExceptionHandler exhand = Thread.getDefaultUncaughtExceptionHandler();

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
				@Override
				public void uncaughtException(Thread thread, Throwable e){
					String message = "Error at "+Calendar.getInstance().getTime()+"!\n";
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					Log.error(message += sw.toString());
					pw.close();

					Context context = getApplicationContext();
					Intent registerActivity = new Intent(context, CrashReport.class);
					registerActivity.putExtra("message", message);
					registerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					registerActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
					context.startActivity(registerActivity);
					
					// make sure we die, otherwise the app will hang ...
					//android.os.Process.killProcess(android.os.Process.myPid());
					//exhand.uncaughtException(thread, e);
					System.exit(1);
				}
			});

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();

        initialize(new wrightway.gdx.galagale.MyGdxGame(), cfg);
    }
}
