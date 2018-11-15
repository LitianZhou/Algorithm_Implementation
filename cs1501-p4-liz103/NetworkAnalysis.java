import java.util.*;
import java.lang.*;
import java.io.*;

class NetworkAnalysis{
	private EdgeWeightedGraph latencyGraph; //use in function 1
	private DijkstraUndirectedSP lowLatencyPath; // function 1
	private KruskalMST kruskalMST; //function 4
	private FlowNetwork flowNet; //function 3
	private FordFulkerson FFson; //function 3
	private int numVertex;
	//constructor
	public NetworkAnalysis() {

	}
	public static void main(String[] args) {
		NetworkAnalysis netAna = new NetworkAnalysis();
		netAna.run(args[0]);
	}

	public void run(String fileName) {
		//Store the data:
		try 
		{
			File file = new File(fileName);
			Scanner sc = new Scanner(file);
			numVertex = sc.nextInt();
			latencyGraph = new EdgeWeightedGraph(numVertex);
			flowNet = new FlowNetwork(numVertex);
			// sc.next(); //consume the newline character
			//buiding graph
			while(sc.hasNext()) {
				int v =sc.nextInt();
				int w =sc.nextInt();
				String type=sc.next();
				int bandwidth = sc.nextInt();
				int length = sc.nextInt();
				double time = 0.0;
				if(type.equals("optical")) time = length/200000000;
				else time = length/230000000;
				latencyGraph.addEdge(new Edge(v, w,time,type,bandwidth,length));
				flowNet.addEdge(new FlowEdge(v,w,bandwidth));
				flowNet.addEdge(new FlowEdge(w,v,bandwidth));

			}
		} 
		catch(FileNotFoundException e) {
			System.err.println("file not found");
		}
		//System.out.println(latencyGraph.toString());
		boolean notFinish = true;
		while(notFinish) {
			System.out.println("_______________________________");
			System.out.println("Welcome to my NetworkAnalysis!");
			System.out.println("Please select from the list:");
			System.out.println("1. Find the lowest latency path");
			System.out.println("2. Copper-connected?");
			System.out.println("3. Maxium bandwidth");
			System.out.println("4. Find the minimum average latency spanning tree");
			System.out.println("5. Check connectness after disconnect one edge");
			System.out.println("6. quit");
			Scanner sc = new Scanner(System.in);
			int operation = sc.nextInt();
			switch(operation) {
				case 1: getLowLatencyPath();
					break;
				case 2: copperConnected();
					break;
				case 3: getMaxBandwidth();
					break;
				case 4: getMinLatencyST();
					break;
				case 5: disconnect();
					break;
				case 6: notFinish = false;
					break;
				}
		}
		System.out.println("Bye");
	}

	public void getLowLatencyPath() {
		System.out.print("~~please type in the start vertex: ");
		Scanner sc = new Scanner(System.in);
		int s = sc.nextInt();
		System.out.print("~~please type in the terminal vertex: ");
		int t = sc.nextInt();
		int bandwidth = 9999999; //to make sure this is not the least bandwidth
		lowLatencyPath = new DijkstraUndirectedSP(latencyGraph, s);
		if(lowLatencyPath.hasPathTo(t)) {
			for(Edge e: lowLatencyPath.pathTo(t)) {
				System.out.print(e.toString()+" ");
				if(bandwidth > e.bandwidth())
					bandwidth = e.bandwidth();
			}
		}
		else System.out.println("There is no such path");
		System.out.println("\nThe bandwidth of this path is: "+bandwidth);
	}

	public void copperConnected() {
		UF uf = new UF(numVertex);
		for(Edge e: latencyGraph.edges()) {
			if(e.type().equals("copper")) {
				uf.union(e.either(), e.iether());
			}
		}
		if(uf.count() == 1) System.out.println("The netWork is connected by copper");
		else 				System.out.println("The netWork is not connected by copper");
	}

	public void getMaxBandwidth() {
		System.out.print("~~please type in the start vertex: ");
		Scanner sc = new Scanner(System.in);
		int s = sc.nextInt();
		System.out.print("~~please type in the terminal vertex: ");
		int t = sc.nextInt();
		FFson = new FordFulkerson(flowNet, s, t);
		System.out.println("The Maxium bandwidth is "+ FFson.value());
	}

	public void getMinLatencyST() {
		kruskalMST = new KruskalMST(latencyGraph);
		for(Edge e: kruskalMST.edges()) {
			System.out.print(e.toString()+" ");		
		}
	}

	public void disconnect() {
		System.out.print("~~please type in the start vertex: ");
		Scanner sc = new Scanner(System.in);
		int s = sc.nextInt();
		System.out.print("~~please type in the terminal vertex: ");
		int t = sc.nextInt();
		UF uf = new UF(numVertex);
		for(Edge e: latencyGraph.edges()) {
			if(!(e.either()==s&&e.iether()==t)||(e.either()==t&&e.iether()==s)) { //if the edge is not disconnected
				uf.union(e.either(), e.iether());
			}
		}
		if(uf.count() == 1) System.out.println("The netWork is still connected");
		else 				System.out.println("The netWork is not connected");
	}
}
