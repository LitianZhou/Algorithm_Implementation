import java.util.*;
import java.io.*;
//import java.io.IOException;

public class ac_test{
	
	private final char Terminator = '^';
	Node rootNode;
	Node historyTrie;
	//constructor
	public ac_test(){
		rootNode = new Node();
		historyTrie = new Node();
	}

	public void run() throws FileNotFoundException, java.io.IOException{
		addFromTxt();
		Scanner kb = new Scanner(System.in);
		char k; //last character of "key"
		ArrayList<Double> times = new ArrayList<Double>();
		do{


			System.out.print("Enter your first letter:");
			String key = "";
			k = kb.next().charAt(0);
			key += k;

			//System.out.println(rootNode.childNode.childNode.peerNode.value);
			while(k!='!' ){
				long t1 = System.nanoTime();
				ArrayList<String> result = dltSearch(key);
				long t2 = System.nanoTime();
				
				times.add((double)(t2 -t1));
				System.out.println("("+ (t2 - t1) + " s)");
				
				
				int resultNum = result.size();
				if(resultNum>0) System.out.println("Predictions:");
				for(int i=1; i<resultNum+1; i++){
					System.out.print("("+i+")"+" "+result.get(i-1)+"	");
				}		
				System.out.println();
				System.out.print("Enter your next letter:");
				k = kb.next().charAt(0);
				if(k=='1'||k=='2'||k=='3'||k=='4'||k=='5'&&result.size() > 0){
					addHistory(result.get(Character.getNumericValue(k)-1));
					System.out.println(" WORD COMPLETE: "+ result.get(Character.getNumericValue(k)-1));
					break;
				}

				if(k == '$'){
					addHistory(key);
					System.out.println(" WORD COMPLETE: "+ key);
					break;
				}

				key += k;
			}
	
		}while(k !='!');

		PrintWriter writer = new PrintWriter("user_history.txt", "UTF-8");
		writer.println("This is my user_history txt file.");
		writer.println("But I don't know how to write it.");
		writer.close();

		System.out.println("Average time:" + (long)aveTime(times)+ "s");
		System.out.println(("BYE"));

	}

	public ArrayList<String> dltSearch(String key) {
		Node cur = rootNode.childNode;
		ArrayList<String> result = new ArrayList<String>();
		int i = 0;
		String curString = "";
		System.out.println(key);
		//System.out.println(cur.value);
		while (cur.value != key.charAt(0)) {
			cur = cur.peerNode;
			//System.out.println(cur.value);
			if (cur == null) {
				return result;
			}
		}
		curString += key.charAt(0);
		Stack<Node> stack = new Stack<Node>();
		int prev = 1;
		//cur = cur.childNode;
		stack.push(cur);
		while (!stack.empty()) {
			if (prev == stack.size()) {
				if (cur.childNode != null) {
					cur = cur.childNode;
					curString += cur.value;
					stack.push(cur);
					prev = stack.size();
					//System.out.println(curString);
					/*if (cur.value == '^' && curString.contains(key)) {
						result.add(curString.substring(0, curString.length()-1));
						stack.push(cur);
						i++;
						if (i == 5) {
							return result;
						}
					}
					else {
						if (curString.length() < key.length() && key.contains(curString)) {
							stack.push(cur);
							prev = stack.size();
						}
						else if (curString.length() >= key.length() && curString.contains(key)) {
							stack.push(cur);
							prev = stack.size();
						}
						else {
							prev = stack.size();
							cur = stack.pop();
							int len = curString.length()-1;
							curString = curString.substring(0, len);
						}
					}*/
				}
				else {
					prev = stack.size();
					cur = stack.pop();
					int len = curString.length()-1;
					curString = curString.substring(0, len);
					if (cur.value == '^' && (curString.indexOf(key) == 0)) {
						result.add(curString);
						//stack.push(cur);
						i++;
						if (i == 5) {
							return result;
						}
					}
				}
			}
			else {
				if (cur.peerNode != null) {
					cur = cur.peerNode;
					curString += cur.value;
					stack.push(cur);
					prev = stack.size();
					//System.out.println(curString);
					/*if (cur.value == '^' && curString.contains(key)) {
						result.add(curString);
						stack.push(cur);
						i++;
						if (i == 5) {
							return result;
						}
					}
					else {
						if (curString.length() < key.length() && key.contains(curString)) {
							stack.push(cur);
							prev = stack.size();
						}
						else if (curString.length() >= key.length() && curString.contains(key)) {
							stack.push(cur);
							prev = stack.size();
						}
						else {
							
							//prev = stack.size();
							//cur = stack.pop();
							int len = curString.length()-1;
							curString = curString.substring(0, len);
						}
					}*/
				}
				else {
					prev = stack.size();
					cur = stack.pop();
					int len = curString.length()-1;
					curString = curString.substring(0, len);
				}
			}
		}
		return result;
	}
	public void addFromTxt() throws FileNotFoundException {
		Scanner sc = new Scanner(new File("Dictionary.txt"));
		//Scanner sc = new Scanner("a ab abc bb");
		while(sc.hasNext()){
			String word = sc.next();
			addWord(word);
		}
	}
	
	public void addWord(String word){
		word = word + Terminator;
		Node currNode = rootNode;

		for(int i=0;i<word.length();i++){
			char c = word.charAt(i);
			currNode = addNode(c, currNode);
		}
	}

	public void addHistory(String word){
		word = word + Terminator;
		Node currNode = historyTrie;

		for(int i=0;i<word.length();i++){
			char c = word.charAt(i);
			currNode = addNode(c, currNode);
		}
		currNode.frequency++;
	}

	public Node addNode(char c, Node parentNode){
		if(parentNode.childNode==null){
			parentNode.childNode = new Node(c);
			return parentNode.childNode;
		}

		Node nextPeer = parentNode.childNode;
		while(nextPeer.peerNode != null){
			if(nextPeer.value == c)
				break;
			nextPeer = nextPeer.peerNode;
			}
			//now pointer is at the Node with same value or the last Node with unchecked value
			//there are two possibilities: 1. last one has the same value; 2. last one has diff value
			if(nextPeer.value == c)
				return nextPeer;

			nextPeer.peerNode = new Node(c);
			return nextPeer.peerNode;
	}
 	
 	public double aveTime(ArrayList<Double> times){
 		Double sum = 0.0;
	  	if(!times.isEmpty()) {
	  	    for (Double time : times) {
	    	    sum += time;
	    	}
			return sum/ times.size();
  		}	
  		return sum;
 	}

	public static void main(String[] args) throws FileNotFoundException, java.io.IOException{
		ac_test letsRool = new ac_test();
		letsRool.run();
	}


}

class Node{
	Node childNode;
	Node peerNode;
	char value;
	int frequency;

	//constructor:
	public Node(){
		childNode = null;
		peerNode = null;
	}

	public Node(char value){
		childNode = null;
		peerNode = null;
		this.value = value;
	}

}