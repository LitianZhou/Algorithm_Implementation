class doublePQ {
	public IndexMinPQ<Integer> pricePQ = new IndexMinPQ<Integer>(100);
	public IndexMinPQ<Integer> milleagePQ = new IndexMinPQ<Integer>(100);

	public doublePQ(){
		//default constructor
	}
	public doublePQ(int carArrInd, int price, int milleage) {
		pricePQ.insert(carArrInd, price);
		milleagePQ.insert(carArrInd, milleage);
	}

	public void add(int carArrInd, int price, int milleage) {
		pricePQ.insert(carArrInd, price);
		milleagePQ.insert(carArrInd, milleage);
	}

	public void changePrice(int targetIndex, int newPrice) {
		pricePQ.changeKey(targetIndex,newPrice);
	}

	public void changeMilleage(int targetIndex, int newMilleage) {
		milleagePQ.changeKey(targetIndex,newMilleage);
	}

	public void remove(int targetIndex) {
		pricePQ.delete(targetIndex);
		milleagePQ.delete(targetIndex);
	}

	public int getLP(){
		return pricePQ.minIndex();
	}

	public int getLM(){
		return milleagePQ.minIndex();
	}
}
