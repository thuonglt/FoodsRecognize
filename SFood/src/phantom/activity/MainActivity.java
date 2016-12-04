package phantom.activity;

import java.util.HashMap;

import phantom.asynctask.CheckFileAsynctask;
import phantom.model.Food;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.phantom.sfood.R;

public class MainActivity extends ActionBarActivity {
	ImageView iv;
	public static String TAG = "MYCAMERA::PHANTOM";
	public static HashMap<Integer, Food> databas = new HashMap<Integer, Food>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Welcome");

		CheckFileAsynctask checkFileAsynctask = new CheckFileAsynctask(this);
		checkFileAsynctask.execute();
	}

	public void onTakePicture(View v) {
		Intent intent = new Intent(this, CameraActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
