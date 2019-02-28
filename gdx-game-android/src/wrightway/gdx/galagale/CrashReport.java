package wrightway.gdx.galagale;

import android.app.*;
import android.content.*;
import android.os.*;

public class CrashReport extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Crash!");
		builder.setMessage(getIntent().getStringExtra("message"));
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2){
					System.exit(0);
				}
		});
		builder.show();
	}
}
