import java.util.*;
import java.lang.*;
import java.io.*;

class carTracker{
	private doublePQ carPQ = new doublePQ();
	private car[] carArr = new car[100];
	private int carArrInd = 0;//startfrom 0, add 1 for each add(), substract 1 for each remove()
	private HashMap<String,doublePQ> model2PQ = new HashMap<String, doublePQ>();
	private HashMap<String, car> VIN2car = new HashMap<String, car>(); 
	private doublePQ[] modesPQ;

	public static void main(String[] args) {
		carTracker carT = new carTracker();
		carT.run();
	}

	public void run() {
		addFromFile();
		boolean done = false;

		while(!done){
			System.out.println("__________________________");
			System.out.println("What would you want to do? Type the number below: ");
			System.out.println("1. Add an new car");
			System.out.println("2. Undate an existed car");
			System.out.println("3. Remove an existed car");
			System.out.println("4. Retrieve the lowest price car");
			System.out.println("5. Retrieve the lowest milleage car");
			System.out.println("6. Retrieve the lowest price car by make and model");
			System.out.println("7. Retrieve the lowest milleage car by make and model");
			System.out.println("0. to exit");
			System.out.println("__________________________");
			
			Scanner sc = new Scanner(System.in);
			int input = sc.nextInt();
			switch(input) {
				case 1: add();
						break;
				case 2: update();
						break;
				case 3: remove();
						break;
				case 4: reLP();
						break;
				case 5: reLM();
						break;
				case 6: reLPbMM();
						break;
				case 7: reLMbMM();
						break;
				case 0: done = true;
						break;
			}
		}
		System.out.println("bye!");
	}

	public void add() {
		System.out.print("Pls input car attributes u want to add:\n");
		System.out.print("car VIN: ");
		Scanner sc = new Scanner(System.in);
		String VIN = sc.next();
		if(VIN2car.containsKey(VIN)){
			System.out.println("The VIN has been added, back to main menu!!");
			return;
		}
		System.out.print("car's make: ");
		String make = sc.next();

		System.out.print("car's model: ");
		String model = sc.next();

		System.out.print("car's price: ");
		int price = sc.nextInt();

		System.out.print("car's mileage: ");
		int milleage = sc.nextInt();

		System.out.print("car's color: ");
		String color = sc.next();
		String makeModel = make+model;//for mode2PQ hashmap use
		//create new car object:
		car theCar = new car(carArrInd, VIN, make, model, price, milleage, color);
		//add car into hashmap vin2car
		VIN2car.put(VIN,theCar);
		//add car into the carArray
		carArr[carArrInd] = theCar;
		//add car into the carPQ
		carPQ.add(carArrInd,price, milleage);
		//add car into the makeModelPQ
		if(!model2PQ.containsKey(makeModel)) {//create new doublePQ for this mode!
			model2PQ.put(makeModel, new doublePQ(carArrInd, price, milleage));
		}
		else if(model2PQ.containsKey(makeModel)) {
			doublePQ existedMode = model2PQ.get(makeModel);
			existedMode.add(carArrInd, price, milleage);
		}

		carArrInd++;
	}

	public void update() {
		System.out.println("Pls input car VIN u want to update:");
		Scanner sc = new Scanner(System.in);
		String VIN = sc.next();
		int targetIndex = VIN2car.get(VIN).getInd(); //get the index of the car
		System.out.println("what attributes do you want to update?");
		System.out.println("1) the price of the car,\n2) the mileage of the car, \n3) the color of the car");
		int updateOpt = sc.nextInt();
		switch(updateOpt){
			case 1: updatePrice(targetIndex);
					break;
			case 2: updateMilleage(targetIndex);
					break;
			case 3: updateColor(targetIndex);
					break;
		}
	}

	public void updatePrice(int targetIndex) {
		System.out.println("pls input the new price: ");
		Scanner sc = new Scanner(System.in);
		int newPrice = sc.nextInt();
		//1. change the price in car object
		carArr[targetIndex].changePrice(newPrice);
		//2. change price in carPQ
		carPQ.changePrice(targetIndex, newPrice);
		//3. change price in makeModelPQ
		String makeModel = carArr[targetIndex].getMM();
		model2PQ.get(makeModel).changePrice(targetIndex, newPrice);
	}

	public void updateMilleage(int targetIndex) {
		System.out.println("pls input the new milleage: ");
		Scanner sc = new Scanner(System.in);
		int newMilleage = sc.nextInt();
		//1. change the price in car object
		carArr[targetIndex].changeMilleage(newMilleage);
		//2. change price in carPQ
		carPQ.changeMilleage(targetIndex, newMilleage);
		//3. change price in makeModelPQ
		String makeModel = carArr[targetIndex].getMM();
		model2PQ.get(makeModel).changeMilleage(targetIndex, newMilleage);
	}

	public void updateColor(int targetIndex) {
		System.out.println("pls input the new milleage: ");
		Scanner sc = new Scanner(System.in);
		String newColor = sc.next();

		carArr[targetIndex].changeColor(newColor);
	}

	public void remove() {
		System.out.print("Pls input car VIN u want to remove:\n");
		Scanner sc = new Scanner(System.in);
		String VIN = sc.next();
		int targetIndex = VIN2car.get(VIN).getInd(); //get the index of the car
		//remove from the modelPQ
		String makeModel = carArr[targetIndex].getMM();
		//remove from carArr in the last step because YOU need it to remove it from the PQs!!!
		carArr[targetIndex] = null;
		//remove from the carPQ
		carPQ.remove(targetIndex);
		model2PQ.get(makeModel).remove(targetIndex);
	}

	public void reLP() {
		System.out.println("This is our lowest price car:\n");
		int retrieveInd = carPQ.getLP();
		print(retrieveInd);
	}
	public void reLM() {
		System.out.println("This is our lowest milleage car:\n");
		int retrieveInd = carPQ.getLM();
		print(retrieveInd);
	}
	public void reLPbMM() {
		System.out.print("please give us the make and model you are looking for lowest price(no space between):\n");
		Scanner sc = new Scanner(System.in);
		String makeModel = sc.next();

		if(!model2PQ.containsKey(makeModel)) {
			System.out.println("Sorry, there is no such makeModel:\n");
		}
		if(model2PQ.containsKey(makeModel)) {
			int retrieveInd = model2PQ.get(makeModel).getLP();
			print(retrieveInd);
		}
	}
	public void reLMbMM() {
		System.out.print("please give us the make and model you are looking for lowest milleage(no space between):\n");
		Scanner sc = new Scanner(System.in);
		String makeModel = sc.next();

		if(!model2PQ.containsKey(makeModel)) {
			System.out.println("Sorry, there is no such makeModel:\n");
		}
		if(model2PQ.containsKey(makeModel)) {
			int retrieveInd = model2PQ.get(makeModel).getLM();
			print(retrieveInd);
		}
	}
	public void print(int retrieveInd) {
		System.out.println("__________________________");
		car foundCar = carArr[retrieveInd];
		System.out.println("Here is the car you are looking for");
		System.out.println("VIN: "+ foundCar.getVIN());
		System.out.println("Make: "+ foundCar.getMake());
		System.out.println("model: "+ foundCar.getModel());
		System.out.println("price: "+ foundCar.getPrice());
		System.out.println("milleage: "+ foundCar.getMilleage());
		System.out.println("color: "+ foundCar.getColor());
		//System.out.println(foundCar.getInd());
	}

	public void addFromFile() {
		try{
			File file = new File("cars.txt");
			Scanner sc = new Scanner(file);
			String[] attr = new String[6];
			sc.nextLine(); //to consume the tilte line
			while(sc.hasNextLine()) {
				attr = sc.nextLine().split(":");
				
				String VIN = attr[0];
				int price = Integer.parseInt(attr[3]);
				int milleage = Integer.parseInt(attr[4]);

				String makeModel = attr[1]+attr[2];
				//create new car object:
				car theCar = new car(carArrInd, VIN, attr[1], attr[2], price, milleage, attr[5]);
				//add car into hashmap vin2car
				VIN2car.put(VIN,theCar);
				//add car into the carArray
				carArr[carArrInd] = theCar;
				
				//add car into the carPQ
				carPQ.add(carArrInd,price, milleage);
				//add car into the makeModelPQ
				if(!model2PQ.containsKey(makeModel)) {//create new doublePQ for this mode!
					model2PQ.put(makeModel, new doublePQ(carArrInd, price, milleage));
				}
				else if(model2PQ.containsKey(makeModel)) {
					doublePQ existedMode = model2PQ.get(makeModel);
					existedMode.add(carArrInd, price, milleage);
				}
				carArrInd++;
			}
		}
		catch(FileNotFoundException e) {
			System.err.println("file not found");
		}
		
	}
}