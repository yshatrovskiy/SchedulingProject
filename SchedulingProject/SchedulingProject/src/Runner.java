import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;


public class Runner {


	static double clock= 0;

	static LinkedList<Integer> inputValues = new LinkedList<Integer>();
	static LinkedList<Process> readyQueue = new LinkedList<Process>();
	static PriorityQueue<Process> cpuQueue = new PriorityQueue<Process>(1);
	static LinkedList<Process> ioQueue = new LinkedList<Process>();
	static LinkedList<Process> ioServQueue = new LinkedList<Process>();
	static PriorityQueue<Event> eventQueue = new PriorityQueue<Event>(20);

	static double 
	cpuProcessCount, 
	ioprocessCount, 
	q, 
	conSwitch, 
	avgProcessLength, 
	avgTimeBetweenProc, 
	perJobIo, 
	avgInterruptTime, 
	totSimTime, 
	contextSwitchTime,
	ioCPUTime,
	cpuCPUTime;
	
	static int 	
	cpuBoundCount, 
	ioBoundCount, 
	createdProcesses, 
	iocpuUseCount,
	cpucpuUseCount;

	public static void main(String args[]) throws FileNotFoundException{


		BufferedReader brTest;
		try {
			String line;
			brTest = new BufferedReader(new FileReader("params1.txt"));
			brTest.readLine();
			while((line = brTest.readLine()) != null){
				inputValues.add(Integer.parseInt(line.replaceAll("[\\D]", "")));
			}

			totSimTime = inputValues.get(0);

			q = inputValues.get(1)/ 1000.0;

			conSwitch = inputValues.get(2)/ 1000.0;

			avgProcessLength = inputValues.get(3)/ 1000.0;

			avgTimeBetweenProc = inputValues.get(4)/ 1000.0;

			perJobIo = inputValues.get(5);

			avgInterruptTime = inputValues.get(6)/ 1000.0;


			//			
			//			System.out.println(totSimTime);
			System.out.println(q);
			System.out.println( "Context Switch " + conSwitch);
			//			System.out.println(avgProcessLength);
			//						System.out.println(avgTimeBetweenProc);
			//						System.out.println(perJobIo);
			//			System.out.println(avgInterruptTime);


		} catch (IOException e1) {
			e1.printStackTrace();
		}


		boolean firstOccurance = true;
		eventQueue.add(new Event(0, "New"));
		createdProcesses++;
		int pid = 0;

		//Reverse Symbol for clock to work
		while(clock < totSimTime){
			printEvents(eventQueue);
			DecimalFormat df = new DecimalFormat("##.000000");
			df.format(clock);

			System.out.print("Clock " + df.format(clock) + " | Event Type : ");
			//			printEvents(eventQueue);
			Event current = eventQueue.poll();	

			//Set Time
			clock = current.getTime();
			switch(current.getType()){

			//Create a new process, add event to create new 
			//process with time stamp of clock + average time between processes
			case "New":
				
				System.out.print("New  | ");
				pid++;
				if(firstOccurance){
					readyQueue.add(new Process(pid, 0, (long)generateRandomTime(avgProcessLength), generateIO(perJobIo)));
					firstOccurance = false;
				}else{
					readyQueue.add(new Process(pid, current.getTime(), generateRandomTime(avgProcessLength), generateIO(perJobIo)));
				}

				//Add event to create new process and event for scheduler
				createdProcesses++;
				eventQueue.add(new Event(clock + generateRandomTime(avgTimeBetweenProc), "New"));
				eventQueue.add(new Event(clock, "Scheduler"));
				break;

			case "Scheduler":
				System.out.print("Scheduler | ");

				//Pop eventQueue and put process on CPU
				//If not empty, event does nothing
				if(!readyQueue.isEmpty() && cpuQueue.isEmpty()){
					Process currentProcess = readyQueue.removeFirst();
					cpuQueue.add(currentProcess);
					
					System.out.println("--------------CPU Time is : " +  currentProcess.getCpuTime() + " Q is " + q + " --------------");
					System.out.print("is CPU Bound: " +currentProcess.isCpuBound() + " | Time Remaining: " + currentProcess.getCpuTime() + " | ID: " +currentProcess.getPid() + " ");


					//If CPU bound, check CPUtime against quantum
					//Check cpuBurst first

					double currentBurst = generateCPUBurst(currentProcess.isCpuBound());
					double contextSwitch = generateRandomTime(conSwitch);
					System.out.println();
					System.out.println("Context Switch Time Generated " + contextSwitch);
					System.out.println();
					contextSwitchTime += contextSwitch;
					
					if(currentBurst > currentProcess.getCpuTime()){

						//Call Done
						double futureTime = (double) (clock + currentProcess.getCpuTime() + contextSwitch);
						System.out.print("CPU Time Less " + " Future Time: " + futureTime);
						eventQueue.add(new Event(futureTime, "Done"));
						System.out.print(" | Calls " + "Done at " + futureTime + " ");
					
					}else{

						if(currentBurst > q){

							//Call Quantum
							int futureTime = (int) (clock + q + contextSwitch);
							System.out.print("Greater than Q " + " Future Time: " + futureTime);
							eventQueue.add(new Event(futureTime, "Quantum"));
							System.out.print(" | Calls " + "Quantum at " + futureTime + " ");

						}else{

							//Call IO
							int futureTime = (int) (clock + currentBurst + contextSwitch);
							System.out.print("Less than Q " + " Future Time: " + futureTime + " with CPU Burst of " + currentBurst);
							eventQueue.add(new Event(futureTime, "IO", currentBurst));
							System.out.print(" | Calls " + "IO at " + futureTime + " ");
						}
					}

				}else{
					System.out.print("No Processes in Queue");
				}


				break;

			case "IO":
				System.out.print("IO | ");
				Process ioProcess = cpuQueue.poll();
				ioProcess.setCpuTime(ioProcess.getCpuTime() - current.getBurstTimeUsed());
				
				//Count CPU Usage
				upDateCPUuse(ioProcess, current.getBurstTimeUsed());
				
				ioQueue.add(ioProcess);
				eventQueue.add(new Event(current.getTime(), "IO Serve"));
				System.out.print(" | Calls " + "IO Serve ");
				break;


			case "IO Serve":
				System.out.print("IO Serve | ");
				Process ioServeProcess = ioQueue.removeFirst();
				double timeAfterProcessing = (generateRandomTime(avgInterruptTime) + current.getTime());
				System.out.print(timeAfterProcessing);
				ioServQueue.add(ioServeProcess);
				eventQueue.add(new Event(timeAfterProcessing, "Back Ready"));
				System.out.print(" | Calls " + "Back Ready ");
				break;

			case "Back Ready":
				System.out.print("Back Ready | ");
				Process readyProcess = ioServQueue.removeFirst();
				readyQueue.add(readyProcess);
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				System.out.print(" | Calls " + "Scheduler ");
				break;

			case "Quantum":
				System.out.print("Quantum | ");
				Process qProcess = cpuQueue.poll();
				System.out.println( "-Before :"+qProcess.getCpuTime() + " ");
				qProcess.setCpuTime(qProcess.getCpuTime() - q);
				
				//Count CPU Usage
				upDateCPUuse(qProcess, q);
				
				System.out.println("-After :"+qProcess.getCpuTime() + "  ");
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				System.out.print(" | Calls " + "Scheduler ");
				break;

			case "Done":
				System.out.print("Done | ");
				Process doneProcess = cpuQueue.poll(); 
				
				
				
				//Count CPU Usage
				upDateCPUuse(doneProcess, doneProcess.getCpuTime());
				
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				addCompleted(doneProcess);
				System.out.print(" | Calls " + "Scheduler ");
				break;

			}
			System.out.println();
		}
		System.out.println();
		System.out.println("Completed IO " + ioBoundCount);
		System.out.println("Completed CPU " + cpuBoundCount);
		System.out.println("Completed Total " + (cpuBoundCount + ioBoundCount));
		System.out.println("Created Total " + createdProcesses);
		System.out.println("Context Switch Total " + contextSwitchTime);
		System.out.println("Average IO Bound CPU Used Time " + ((double)ioCPUTime/iocpuUseCount));
		System.out.println("Average CPU Bound CPU Used Time " + ((double)cpuCPUTime/cpucpuUseCount));
		System.out.println("Average Overall CPU Used Time " + ((double) (cpuCPUTime + ioCPUTime)/(cpucpuUseCount + iocpuUseCount)));
		
	}

	//Random Number Generated Based on Input
	public static double generateRandomTime(double avgTimeBetweenProc2){
		return (double) ((avgTimeBetweenProc2/2) + (Math.random() * avgTimeBetweenProc2));
	}

	//Percent CPU/IO Generator
	public static boolean generateIO(double percent){
		Random rand = new Random();
		double tester = rand.nextInt(101);
		//		System.out.println();
		//		System.out.println("Percent Entered: " + percent + " Result Number: " + tester);
		if(tester > percent)
			return true;
		else
			return false;
	}

	public static void printEvents(PriorityQueue<Event> eventQueue){
		System.out.println();
		System.out.println("--Event Queue--");
		for (Event event: eventQueue){
			System.out.print(event.toString() + " | ");
		}
		System.out.println();
	}
	
	public static void upDateCPUuse(Process process, double timeUsed){
		if(process.cpuBound){
			cpucpuUseCount++;
			cpuCPUTime+= timeUsed;
		}else{
			iocpuUseCount++;
			ioCPUTime += timeUsed;
		}
		
		
	}

	public static double generateCPUBurst(boolean cpuBound){
		Random rand = new Random();
		if(cpuBound)
			return  rand.nextInt((10001) + 10000 / 1000);
		else
			return  rand.nextInt((2001) + 2000) / 1000;
	}
	
	public static void addCompleted(Process p){
		if(p.cpuBound){
			cpuBoundCount++;
		}else{
			ioBoundCount++;
		}
		
	}
}
