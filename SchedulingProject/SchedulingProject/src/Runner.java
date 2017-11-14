import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;


public class Runner {
	
	
	static int clock= 0;

	static LinkedList<Integer> inputValues = new LinkedList<Integer>();
	static LinkedList<Process> readyQueue = new LinkedList<Process>();
	static LinkedList<Process> cpuQueue = new LinkedList<Process>();
	static LinkedList<Process> ioQueue = new LinkedList<Process>();
	static LinkedList<Process> ioServQueue = new LinkedList<Process>();
	
	
//	PriorityQueue(11, Comparator<? super E> comparator);
//	
//    PriorityQueue<String> queue = new PriorityQueue<String>(10, comparator);
	
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
		

		while(clock < totSimTime){
			
			createNewProcess();
			
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
	
	public static int generateRandomTime(int averageMicroProvided){
		return (averageMicroProvided/2) + (int) (Math.random() * averageMicroProvided);
	}
	
	public static void createNewProcess(){
		//Check if process is first .... maybe
		//clock only gets updated in this method
		
		int timeCreated = clock + generateRandomTime(avgTimeBetweenProc);
		Process first = new Process(timeCreated, generateRandomTime(avgProcLen), false);
		pushToReadyQueue(first);
	}
	
	public static void pushToReadyQueue(Process process){
		
		readyQueue.add(process);
		if(checkCPU()){
			cpuQueue.add(process);
		}
		readyQueue.remove(process);
	}
	
	public static void pushToCPU(Process process){
		process.cpuTime -= q;
		clock += q;
		endCpuBurst(process);
	}
	
	public static boolean checkCPU(){
		return cpuQueue.isEmpty();
	}
	
	public static void endCpuBurst(Process process){
		if(process.cpuTime != 0){
			
		}
	}
	
	public static void pushToIOServ(Process process){
		
	}
	
	public static void pushToDone(Process process){
		
	}
	
	public static void checkTime(Process process){
		
	}
	
	
	

}
