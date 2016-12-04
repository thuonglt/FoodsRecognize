package phantom.activity;

import java.io.File;

import phantom.asynctask.PredicImageAsynctask;
import phantom.model.Food;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.phantom.sfood.R;

public final class LabActivity extends ActionBarActivity {

	public static final String PHOTO_FILE_EXTENSION = ".png";
	public static final String PHOTO_MIME_TYPE = "image/png";

	public static final String EXTRA_PHOTO_URI = "com.phantom.LabActivity.extra.PHOTO_URI";
	public static final String EXTRA_PHOTO_DATA_PATH = "com.phantom.LabActivity.extra.PHOTO_DATA_PATH";
	static String root_folder = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + "phantom" + File.separator;
	public static String PATH_DATA_SVM = root_folder
			+ "Food_file_data_training_SURF.xml";;
	public static String PATH_DATA_BOW = root_folder + "BOW_SURF.DAT";

	private Uri mUri;
	private String mDataPath;

	public ImageView imageView;
	public TextView tvView;
	public static Food myFood = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab);
		// --------------------------
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("Food Detail");
		// actionBar.setBackgroundDrawable(new ColorDrawable(Color
		// .parseColor("#3B5998")));
		// ------------------------------

		final Intent intent = getIntent();
		mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI);
		mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);

		imageView = (ImageView) findViewById(R.id.image);
		tvView = (TextView) findViewById(R.id.txtDetail);
		imageView.setImageURI(mUri);

		PredicImageAsynctask asynctask = new PredicImageAsynctask(this);
		asynctask.execute(mDataPath);

	}

	// --------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.activity_lab, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete:
			deletePhoto();
			return true;
		case R.id.menu_edit:
			editPhoto();
			return true;
		case R.id.menu_share:
			sharePhoto();
			return true;
		case R.id.menu_finish:
			onBackPressed();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * Show a confirmation dialog. On confirmation ("Delete"), the photo is
	 * deleted and the activity finishes.
	 */
	private void deletePhoto() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(
				LabActivity.this);
		alert.setTitle("Delete photo!");
		alert.setMessage("Do you want delete this photo?");
		alert.setCancelable(false);
		alert.setPositiveButton("Delete",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						getContentResolver().delete(
								Images.Media.EXTERNAL_CONTENT_URI,
								MediaStore.MediaColumns.DATA + "=?",
								new String[] { mDataPath });
						finish();
					}
				});
		alert.setNegativeButton(android.R.string.cancel, null);
		alert.show();
	}

	/*
	 * Show a chooser so that the user may pick an app for editing the photo.
	 */
	private void editPhoto() {
		final Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setDataAndType(mUri, PHOTO_MIME_TYPE);
		startActivity(Intent.createChooser(intent, "Chose Editor"));

	}

	public void onClickMoreDetail(View v) {
		// Intent intent = new Intent(LabActivity.this,
		// DetailFoodActivity.class);
		// startActivity(intent);
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.dialog_input_prompt, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput);
		userInput.setFocusable(true);

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// get user input and set it to result
						int dataWeight = Integer.parseInt(userInput.getText()
								.toString().trim());

						Intent intent = new Intent(LabActivity.this,
								DetailFoodActivity.class);
						intent.putExtra(LabActivity.EXTRA_PHOTO_URI, mUri);
						intent.putExtra("DATA_WEIGHT", dataWeight);
						startActivity(intent);
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	/*
	 * Show a chooser so that the user may pick an app for sending the photo.
	 */
	private void sharePhoto() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType(PHOTO_MIME_TYPE);
		intent.putExtra(Intent.EXTRA_STREAM, mUri);
		intent.putExtra(Intent.EXTRA_SUBJECT, "My Photo From Phantom!");
		intent.putExtra(Intent.EXTRA_TEXT, "Hello World!");
		startActivity(Intent.createChooser(intent, "Chose app share"));
	}
}
