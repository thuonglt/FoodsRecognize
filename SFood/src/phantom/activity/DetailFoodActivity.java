package phantom.activity;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Mat;

import phantom.model.Food;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract.Helpers;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.phantom.sfood.R;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;
import com.samsung.android.sdk.healthdata.HealthUserProfile;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionResult;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;

public class DetailFoodActivity extends ActionBarActivity {
	int data_weight = 0;
	private final int MENU_ITEM_PERMISSION_SETTING = 1;
	public static final String APP_TAG = MainActivity.TAG;
	private static DetailFoodActivity mInstance = null;
	private HealthDataStore mStore;
	private HealthConnectionErrorResult mConnError;
	private Set<PermissionKey> mKeySet;
	HealthUserProfile profile = null;
	TextView tvSex, tvAge, tvWeight, tvHeight;
	TextView tvFat, tvCab, tvProtein, tvCholesteron, tvSodium, tvPotassiuml,
			tvDescription, tvBMI, tvCalo;
	private Uri mUri;
	private String mDataPath;
	private float scale = 1;
	String status_body = "Chỉ số BMI cho thấy cơ thể bạn đang trong trạng thái ";

	public static final String EXTRA_PHOTO_URI = "com.phantom.LabActivity.extra.PHOTO_URI";
	public static final String EXTRA_PHOTO_DATA_PATH = "com.phantom.LabActivity.extra.PHOTO_DATA_PATH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_food);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("Welcome");

		mUri = getIntent().getParcelableExtra(EXTRA_PHOTO_URI);
		mDataPath = getIntent().getStringExtra(EXTRA_PHOTO_DATA_PATH);
		data_weight = getIntent().getIntExtra("DATA_WEIGHT", 0);
		scale = (float) data_weight / 100.0f;
		mInstance = this;
		mKeySet = new HashSet<PermissionKey>();
		mKeySet.add(new PermissionKey(HealthConstants.USER_PROFILE_DATA_TYPE,
				PermissionType.READ));
		HealthDataService healthDataService = new HealthDataService();
		try {
			healthDataService.initialize(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create a HealthDataStore instance and set its listener
		mStore = new HealthDataStore(this, mConnectionListener);
		// Request the connection to the health data store
		mStore.connectService();
		// readData();

	}

	public void innitData(HealthUserProfile profile) {
		System.out.println("XXXXXXXX" + profile.getBirthDate());
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setImageURI(mUri);

		tvAge = (TextView) findViewById(R.id.tvAge);
		tvSex = (TextView) findViewById(R.id.tvSex);
		tvWeight = (TextView) findViewById(R.id.tvWeight);
		tvHeight = (TextView) findViewById(R.id.tvHeight);
		tvBMI = (TextView) findViewById(R.id.tvBmi);
		tvFat = (TextView) findViewById(R.id.tvFat);
		tvCholesteron = (TextView) findViewById(R.id.tvCholesterol);
		tvPotassiuml = (TextView) findViewById(R.id.tvPotassium);
		tvProtein = (TextView) findViewById(R.id.tvProtein);
		tvSodium = (TextView) findViewById(R.id.tvSodium);
		tvCab = (TextView) findViewById(R.id.tvCarbohydrate);
		tvDescription = (TextView) findViewById(R.id.tvDescription);
		tvCalo = (TextView) findViewById(R.id.tvCalorie);

		Food myFood = LabActivity.myFood;
		String _age = profile.getBirthDate().substring(0, 4);
		int age = new Date().getYear() - (Integer.parseInt(_age) - 1900);
		System.out.println(_age + "   " + new Date().getYear());
		tvAge.setText(age + " ");
		tvSex.setText(((profile.getGender() == 1) ? "Nam" : "Nữ"));
		tvWeight.setText(profile.getWeight() + " Kg");
		tvHeight.setText(profile.getHeight() + " Cm");
		float h = profile.getHeight() / 100;
		float bmi = profile.getWeight() / (h * h);
		tvBMI.setText(Math.round(bmi * 10.0) / 10.0 + "");

		tvFat.setText(tvFat.getText().toString() + " : "
				+ Math.round(myFood.fat * scale * 10) / 10 + " g");
		tvCab.setText(tvCab.getText().toString() + " : "
				+ Math.round(myFood.carbohydrate * scale * 10) / 10 + " g");
		tvProtein.setText(tvProtein.getText().toString() + " : "
				+ Math.round(myFood.protein * scale * 10) / 10 + " g");
		tvCholesteron.setText(tvCholesteron.getText().toString() + " : "
				+ Math.round(myFood.cholesterol * scale * 10) / 10 + " mg");
		tvSodium.setText(tvSodium.getText().toString() + " : " + myFood.sodium
				* scale + " mg");
		tvPotassiuml.setText(tvPotassiuml.getText().toString() + " : "
				+ Math.round(myFood.postassium * scale * 10) / 10 + " mg");
		tvCalo.setText(tvCalo.getText().toString() + "  "
				+ Math.round(myFood.calorie * scale * 10) / 10 + " kcal");

		if (bmi < 15.5f) {
			status_body = status_body + "thiếu cân!";
		} else if (bmi >= 18.5f && bmi < 22.9f) {
			status_body = status_body + "bình thường!";
		} else if (bmi >= 22.9f && bmi < 25) {
			status_body = status_body + "thừa cân!";
		} else {
			status_body = status_body + "béo phì!";
		}
		tvDescription.setText(status_body);

	}

	private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {

		@Override
		public void onConnected() {
			Log.d(APP_TAG, "Health data service is connected.");
			HealthPermissionManager pmsManager = new HealthPermissionManager(
					mStore);

			try {
				// Check whether the permissions that this application needs are
				// acquired
				Map<PermissionKey, Boolean> resultMap = pmsManager
						.isPermissionAcquired(mKeySet);

				if (resultMap.containsValue(Boolean.FALSE)) {
					// Request the permission for reading step counts if it is
					// not acquired
					pmsManager.requestPermissions(mKeySet).setResultListener(
							mPermissionListener);
				} else {
					// Get the current step count and display it
					// readData();
					System.out.println("hello phantom! connected to shealth!");
					// float wei = HealthUserProfile.getProfile(mStore)
					// .getHeight();
					// System.out.println("xxxxxxxxx" + wei);
					profile = HealthUserProfile.getProfile(mStore);
					innitData(profile);
				}
			} catch (Exception e) {
				Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
				Log.e(APP_TAG, "Permission setting fails.");
				e.printStackTrace();
			}
		}

		@Override
		public void onConnectionFailed(HealthConnectionErrorResult error) {
			Log.d(APP_TAG, "Health data service is not available.");
			showConnectionFailureDialog(error);
		}

		@Override
		public void onDisconnected() {
			Log.d(APP_TAG, "Health data service is disconnected.");
		}
	};

	private final HealthResultHolder.ResultListener<PermissionResult> mPermissionListener = new HealthResultHolder.ResultListener<PermissionResult>() {

		@Override
		public void onResult(PermissionResult result) {
			Log.d(APP_TAG, "Permission callback is received.");
			Map<PermissionKey, Boolean> resultMap = result.getResultMap();

			if (resultMap.containsValue(Boolean.FALSE)) {
				// drawStepCount("");
				// showPermissionAlarmDialog();
			} else {
				// Get the current step count and display it
				// readData();
			}
		}
	};

	private void showConnectionFailureDialog(HealthConnectionErrorResult error) {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		mConnError = error;
		String message = "Connection with S Health is not available";

		if (mConnError.hasResolution()) {
			switch (error.getErrorCode()) {
			case HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED:
				message = "Please install S Health";
				break;
			case HealthConnectionErrorResult.OLD_VERSION_PLATFORM:
				message = "Please upgrade S Health";
				break;
			case HealthConnectionErrorResult.PLATFORM_DISABLED:
				message = "Please enable S Health";
				break;
			case HealthConnectionErrorResult.USER_AGREEMENT_NEEDED:
				message = "Please agree with S Health policy";
				break;
			default:
				message = "Please make S Health available";
				break;
			}
		}

		alert.setMessage(message);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				if (mConnError.hasResolution()) {
					mConnError.resolve(mInstance);
				}
			}
		});

		if (error.hasResolution()) {
			alert.setNegativeButton("Cancel", null);
		}

		alert.show();
	}

	@Override
	public void onDestroy() {
		mStore.disconnectService();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(1, MENU_ITEM_PERMISSION_SETTING, 0, "Connect to S Health");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		} else if (item.getItemId() == (MENU_ITEM_PERMISSION_SETTING)) {
			HealthPermissionManager pmsManager = new HealthPermissionManager(
					mStore);
			try {
				// Show user permission UI for allowing user to change options
				pmsManager.requestPermissions(mKeySet).setResultListener(
						mPermissionListener);
			} catch (Exception e) {
				Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
				Log.e(APP_TAG, "Permission setting fails.");
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
