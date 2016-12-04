package phantom.asynctask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import phantom.activity.LabActivity;
import phantom.activity.MainActivity;
import phantom.model.Food;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class CheckFileAsynctask extends AsyncTask<Void, Void, Void> {
	Context context;
	AssetManager assetManager;

	public CheckFileAsynctask(Context context) {
		this.context = context;
		assetManager = context.getAssets();
	}

	@Override
	protected Void doInBackground(Void... params) {
		File file_bow = new File(LabActivity.PATH_DATA_BOW);
		File file_svm = new File(LabActivity.PATH_DATA_SVM);
		String root_folder = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "phantom"
				+ File.separator;
		Log.i(MainActivity.TAG, file_bow.exists() + " -- " + file_svm.exists());
		if (file_bow.exists() == false || file_svm.exists() == false) {
			Log.i(MainActivity.TAG, "Crate folder!");
			File folder = new File(root_folder);
			folder.mkdir();
			try {
				writeFile(assetManager.open("BOW_SURF.DAT"),
						LabActivity.PATH_DATA_BOW);
				writeFile(
						assetManager.open("Food_file_data_training_SURF.xml"),
						LabActivity.PATH_DATA_SVM);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (MainActivity.databas.size() < 10) {
			loadDatabase();
		}

		return null;
	}

	public void loadDatabase() {
		try {
			Scanner sc = new Scanner(context.getAssets().open("DATABASE.DAT"));
			while (sc.hasNext()) {
				String data = sc.nextLine();
				System.out.println(data);
				String[] food = data.split("\t");
				int id = Integer.parseInt(food[0].trim());
				String name = food[1];
				float calorie = Float.parseFloat(food[2].trim());
				float fat = Float.parseFloat(food[3].trim());
				float cholesterol = Float.parseFloat(food[4].trim());
				float sodium = Float.parseFloat(food[5].trim());
				float postassium = Float.parseFloat(food[6].trim());
				float carbohydrate = Float.parseFloat(food[7].trim());
				float protein = Float.parseFloat(food[8].trim());

				Food dataFood = new Food(id, name, calorie, fat, cholesterol,
						sodium, postassium, carbohydrate, protein);
				MainActivity.databas.put(dataFood.id, dataFood);
			}
			sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeFile(InputStream stream, String path) {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = stream;
			output = new FileOutputStream(path);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			input.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
