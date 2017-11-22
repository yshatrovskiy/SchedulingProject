import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;


public class Runner {


	static int clock= 0;

	static LinkedList<Integer> inputValues = new LinkedList<Integer>();
	static LinkedList<Process> readyQueue = new LinkedList<Process>();
	static LinkedList<Process> cpuQueue = new LinkedList<Process>();
	static LinkedList<Process> ioQueue = new LinkedList<Process>();
	static LinkedList<Process> ioServQueue = new LinkedList<Process>();
	static PriorityQueue<Event> eventQueue = new PriorityQueue<Event>(20);

	static int cpuProcessCount, ioprocessCount, q, 
	totSimTime, conSwitch, avgProcLen, avgTimeBetweenProc, 
	perJobIo, avgInterruptTime;


	public static void main(String args[]) throws FileNotFoundException{
		

		BufferedReader brTest;
		try {
			String line;
			brTest = new BufferedReader(new FileReader("params1.txt"));
			brTest.readLine();
			while((line = brTest.readLine()) != null){
				inputValues.add(Integer.parseInt(line.replaceAll("[\\D]", "")));
			}

			totSimTime = inputValues.get(1);
			q = inputValues.get(0) * 1000000;
			conSwitch = inputValues.get(2);
			avgProcLen = inputValues.get(3);
			avgTimeBetweenProc = inputValues.get(4);
			perJobIo = inputValues.get(5);
			avgInterruptTime = inputValues.get(6);		

			//Average Tester
			int temp = 0;
			for(int i = 0; i<100;i++){
				temp += generateRandomTime(avgInterruptTime);
			}
			System.out.println(temp/100);


		} catch (IOException e1) {
			e1.printStackTrace();

		}
		
		//Priority Queue Test Input/Output
		for(int i = 0; i < 10; i++){
			eventQueue.add(new Event(generateRandomTime(100), "First " + i));
			System.out.print(generateRandomTime(100) + " ,");
		}
		System.out.println("Done One");
		for(int i = 0; i < 10; i++){
			System.out.println(eventQueue.peek().getTime());
			eventQueue.remove();
		}
		System.out.println("Done Two");
		

		//Reverse Symbol for clock to work
		while(clock > totSimTime){
			String type = "test";  //Deque type from priorityQueue

			switch(type){

			case "New":
				//Add new process to queue
				break;

			case "Scheduler":
				//Pop eventQueue and put process on CPU
				break;

			}

			//createNewProcess()
			//pushToCPU()
			//--cpu queue
			//endCpuBurst()
			//pushToReadyQueue
			//pushToIOServ
			//pushToDone
			//checkTime()
			//--see if new process or to cpu

		}

	}

	//Random Number Generated Based on Input
	public static int generateRandomTime(int averageMicroProvided){
		return (averageMicroProvided/2) + (int) (Math.random() * averageMicroProvided);
	}
	
	//Percent CPU/IO Generator
	public static boolean generateIO(int percent){
		Random rand = new Random();
		int tester = rand.nextInt(101);
		System.out.println("tester is " +tester);
		if(tester <= percent)
			return true;
		else
			return false;
	}
}
