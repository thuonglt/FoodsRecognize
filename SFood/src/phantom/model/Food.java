package phantom.model;

import java.io.Serializable;

public class Food implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String name;
	public float calorie, fat, cholesterol, sodium, postassium, carbohydrate,
			protein;

	public Food() {
	}

	public Food(int id, String name, float calorie, float fat,
			float cholesterol, float sodium, float postassium,
			float carbohydrate, float protein) {
		super();
		this.id = id;
		this.name = name;
		this.calorie = calorie;
		this.fat = fat;
		this.cholesterol = cholesterol;
		this.sodium = sodium;
		this.postassium = postassium;
		this.carbohydrate = carbohydrate;
		this.protein = protein;
	}

}
