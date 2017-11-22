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
	totSimTime, conSwitch, avgProcessLength, avgTimeBetweenProc, 
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
			
			totSimTime = inputValues.get(1) * 1000000;
			q = inputValues.get(0) * 10000;
			conSwitch = inputValues.get(2);
			avgProcessLength = inputValues.get(3);
			avgTimeBetweenProc = inputValues.get(4);
			perJobIo = inputValues.get(5);
			avgInterruptTime = inputValues.get(6);		


		} catch (IOException e1) {
			e1.printStackTrace();

		}

		boolean firstOccurance = true;
		eventQueue.add(new Event(generateRandomTime(avgTimeBetweenProc), "New"));

		//Reverse Symbol for clock to work
		while(clock < totSimTime){
			
			Event current = eventQueue.poll();	
			switch(current.getType()){
			
			//Create a new process, add event to create new 
			//process with time stamp of clock + average time between processes
			case "New":
				
				if(firstOccurance){
					readyQueue.add(new Process(0, generateRandomTime(avgProcessLength), generateIO(perJobIo)));
					firstOccurance = false;
				}else{
					readyQueue.add(new Process(current.getTime(), generateRandomTime(avgProcessLength), generateIO(perJobIo)));
				}
				
				//Add event to create new process and event for scheduler
				eventQueue.add(new Event(clock + generateRandomTime(avgTimeBetweenProc), "New"));
				eventQueue.add(new Event(clock, "Scheduler"));
				
			case "Scheduler":

				//Pop eventQueue and put process on CPU
				if(!readyQueue.isEmpty() && !cpuQueue.isEmpty()){
					Process currentProcess = readyQueue.removeFirst();
					cpuQueue.add(currentProcess);
					
					if(currentProcess.getcpuBurst() <= q){
						if(currentProcess.getCpuTime() <= currentProcess.getcpuBurst()){
							clock += currentProcess.getCpuTime();
							//Event Removed
						}
						else{
							clock += currentProcess.getcpuBurst();
							//Event Added to ready queue
						}
					}else{
						clock += q;
						currentProcess.setCpuTime(currentProcess.getCpuTime() - q);
						//Event Added to ready queue
					}
				}
			}
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
		if(tester <= percent)
			return true;
		else
			return false;
	}
}
