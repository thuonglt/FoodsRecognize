package phantom.database;

import java.util.ArrayList;

import phantom.model.Food;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "MyFood.db";
	public static final String FOODS_TABLE_NAME = "nutrition_facts";
	public static final String FOODS_COLUMN_ID = "id";
	public static final String FOODS_COLUMN_NAME = "name";
	public static final String FOODS_COLUMN_CALO = "calorie";
	public static final String FOODS_COLUMN_FAT = "totalfat";
	public static final String FOODS_COLUMN_CHOLESTEROL = "cholesterol";
	public static final String FOODS_COLUMN_SODIUM = "sodium";
	public static final String FOODS_COLUMN_POTASSIUM = "potassium";
	public static final String FOODS_COLUMN_CARB = "totalcarbohydrate";
	public static final String FOODS_COLUMN_PROTEIN = "protein";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table "
				+ FOODS_TABLE_NAME
				+ "(id INTEGER PRIMARY KEY NOT NULL,name TEXT, calorie REAL,totalfat REAL,cholesterol REAL, sodium REAL,potassium REAL,totalcarbohydrate REAL,protein REAL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + FOODS_TABLE_NAME);
		onCreate(db);
	}

	public boolean insertFood(Food food) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(FOODS_COLUMN_ID, food.id);
		contentValues.put(FOODS_COLUMN_NAME, food.name);
		contentValues.put(FOODS_COLUMN_CALO, food.calorie);
		contentValues.put(FOODS_COLUMN_CHOLESTEROL, food.cholesterol);
		contentValues.put(FOODS_COLUMN_SODIUM, food.sodium);
		contentValues.put(FOODS_COLUMN_POTASSIUM, food.postassium);
		contentValues.put(FOODS_COLUMN_CARB, food.carbohydrate);
		contentValues.put(FOODS_COLUMN_FAT, food.fat);
		db.insert(FOODS_TABLE_NAME, null, contentValues);
		return true;
	}

	public Food getData(int id_food) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from " + FOODS_TABLE_NAME
				+ " where id=" + id_food + "", null);
		res.moveToFirst();
		int id = res.getInt(res.getColumnIndex(FOODS_COLUMN_ID));
		String name = res.getString(res.getColumnIndex(FOODS_COLUMN_NAME));
		float calorie = res.getFloat(res.getColumnIndex(FOODS_COLUMN_CALO));
		float carb = res.getFloat(res.getColumnIndex(FOODS_COLUMN_CARB));
		float cholesterol = res.getFloat(res
				.getColumnIndex(FOODS_COLUMN_CHOLESTEROL));
		float fat = res.getFloat(res.getColumnIndex(FOODS_COLUMN_FAT));
		float pot = res.getFloat(res.getColumnIndex(FOODS_COLUMN_POTASSIUM));
		float so = res.getFloat(res.getColumnIndex(FOODS_COLUMN_SODIUM));
		float protein = res.getFloat(res.getColumnIndex(FOODS_COLUMN_PROTEIN));
		Food food = new Food(id, name, calorie, fat, cholesterol, so, pot,
				carb, protein);

		return food;
	}

	public int numberOfRows() {
		SQLiteDatabase db = this.getReadableDatabase();
		int numRows = (int) DatabaseUtils.queryNumEntries(db, FOODS_TABLE_NAME);
		return numRows;
	}

	public ArrayList<Food> getAllFoods() {
		ArrayList<Food> array_list = new ArrayList<Food>();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from " + FOODS_TABLE_NAME, null);
		res.moveToFirst();

		while (res.isAfterLast() == false) {
			int id = res.getInt(res.getColumnIndex(FOODS_COLUMN_ID));
			String name = res.getString(res.getColumnIndex(FOODS_COLUMN_NAME));
			float calorie = res.getFloat(res.getColumnIndex(FOODS_COLUMN_CALO));
			float carb = res.getFloat(res.getColumnIndex(FOODS_COLUMN_CARB));
			float cholesterol = res.getFloat(res
					.getColumnIndex(FOODS_COLUMN_CHOLESTEROL));
			float fat = res.getFloat(res.getColumnIndex(FOODS_COLUMN_FAT));
			float pot = res
					.getFloat(res.getColumnIndex(FOODS_COLUMN_POTASSIUM));
			float so = res.getFloat(res.getColumnIndex(FOODS_COLUMN_SODIUM));
			float protein = res.getFloat(res
					.getColumnIndex(FOODS_COLUMN_PROTEIN));
			Food food = new Food(id, name, calorie, fat, cholesterol, so, pot,
					carb, protein);
			array_list.add(food);
			res.moveToNext();
		}
		return array_list;
	}
}
