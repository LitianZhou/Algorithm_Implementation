class car {
	private int carArrInd;
	private String VIN;
	private String make;
	private String model;
	private int price;
	private int milleage;
	private String color;

	public car(int carArrInd, String VIN, String make, String model, int price, int milleage, String color) {
		this.carArrInd = carArrInd;
		this.VIN = VIN;
		this.make = make;
		this.model = model;
		this.price = price;
		this.milleage = milleage;
		this.color = color;
	}
	public int getInd() {
		return carArrInd;
	}
	
	public String getVIN() {
		return VIN;
	}

	public String getMake() {
		return make;
	}

	public String getModel() {
		return model;
	}

	public int getPrice() {
		return price;
	}

	public int getMilleage() {
		return milleage;
	}

	public String getColor() {
		return color;
	}

	public void changePrice(int newPrice) {
		this.price = newPrice;
	}

	public void changeMilleage(int newMilleage) {
		this.milleage = newMilleage;
	}

	public void changeColor(String newColor) {
		this.color = newColor;
	}

	public String getMM() {
		return make+model;
	}
}