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
	cpuProcessCount = 0, 
	ioProcessCount = 0, 
	q, 
	conSwitch, 
	avgProcessLength, 
	avgTimeBetweenProc, 
	perJobIo, 
	avgInterruptTime, 
	totSimTime, 
	contextSwitchTime,
	ioCPUTime = 0,
	cpuCPUTime = 0,
	cpuIOTime = 0,
	ioIOTime = 0,	
	cpuBoundCount = 0, 
	ioBoundCount = 0, 
	iocpuUseCount = 0,
	cpucpuUseCount = 0,
	cpuIOCount = 0,
	ioIOCount = 0,
	ioTurnAroundTime = 0,
	cpuTurnAroundTime = 0,
	ioInterCount,
	cpuInterCount,
	totalWaitTime;

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
			q = inputValues.get(1)/ 1000000.0;
			conSwitch = inputValues.get(2)/ 1000000.0;
			avgProcessLength = inputValues.get(3)/ 1000000.0;
			avgTimeBetweenProc = inputValues.get(4)/ 1000000.0;
			perJobIo = inputValues.get(5);
			avgInterruptTime = inputValues.get(6)/ 1000000.0;

		} catch (IOException e1) {
			e1.printStackTrace();
		}


		boolean firstOccurance = true;
		eventQueue.add(new Event(0, "New"));
		int pid = 0;

		DecimalFormat df = new DecimalFormat("##.000000");

		//Reverse Symbol for clock to work
		while(clock < totSimTime){
			printEvents(eventQueue);


			System.out.print("Running Event : ");
			Event current = eventQueue.poll();	

			//Set Time
			clock = current.getTime();
			switch(current.getType()){

			//Create a new process, add event to create new 
			//process with time stamp of clock + average time between processes
			case "New":

				//				System.out.print("New  | ");
				pid++;
				if(firstOccurance){
					Process first = new Process(pid, 0, generateRandomTime(avgProcessLength), generateIO(perJobIo));
					first.setFirstBurst(generateCPUBurst(first.isCpuBound()));
					readyQueue.add(first);
					first.setTimeEnteredWaiting(current.getTime());
					addProcessCount(first);
					firstOccurance = false;
				}else{
					Process second = new Process(pid, current.getTime(), generateRandomTime(avgProcessLength), generateIO(perJobIo));
					second.setFirstBurst(generateCPUBurst(second.isCpuBound()));
					second.setTimeEnteredWaiting(current.getTime());
					addProcessCount(second);
					readyQueue.add(second);
				}

				//Add event to create new process and event for scheduler
				eventQueue.add(new Event(clock + generateRandomTime(avgTimeBetweenProc), "New"));
				eventQueue.add(new Event(clock, "Scheduler"));
				System.out.print(" | --> " + "Scheduler at " + clock + " ");
				break;

			case "Scheduler":
				System.out.print("Scheduler | ");

				//Pop eventQueue and put process on CPU
				//If not empty, event does nothing
				if(!readyQueue.isEmpty() && cpuQueue.isEmpty()){
					Process currentProcess = readyQueue.removeFirst();
					totalWaitTime += current.getTime() - currentProcess.getTimeEnteredWaiting();
					currentProcess.setTimeEnteredWaiting(0);
					cpuQueue.add(currentProcess);

					System.out.print("CPU Time Remaining is : " +  currentProcess.getCpuTime() + " | Q is " + q + " | ");
					System.out.println("is CPU Bound: " +currentProcess.isCpuBound() + " | ID: " +currentProcess.getPid() + " ");


					//If CPU bound, check CPUtime against quantum
					//Check cpuBurst first

					double currentBurst = currentProcess.getFirstBurst();
					double contextSwitch = generateRandomTime(conSwitch);
					System.out.print("Current Burst is " + currentBurst + " | " + "Context Switch is " + contextSwitch +" | ");
					contextSwitchTime = contextSwitchTime + contextSwitch;

					if(currentBurst > currentProcess.getCpuTime()  && q > currentProcess.getCpuTime()){

						//Call Done
						double futureTime = (double) (clock + currentProcess.getCpuTime() + contextSwitch);
						System.out.print("CPU Time Less ");
						eventQueue.add(new Event(futureTime, "Done"));
						System.out.print(" | --> " + "Done at " + futureTime + " ");
//						addInterCount(currentProcess);

					}else{

						if(currentBurst > q){

//							addInterCount(currentProcess);
							//Call Quantum
							
							double futureTime = (double) (clock + q + contextSwitch);
							System.out.print("Greater than Q ");
							eventQueue.add(new Event(futureTime, "Quantum"));
							currentProcess.setFirstBurst(currentProcess.getFirstBurst()-q);
							System.out.print(" | --> " + "Quantum at " + futureTime + " ");

						}else{

							//Call IO
							addInterCount(currentProcess);
							
							double futureTime = (double) (clock + currentBurst + contextSwitch);
							System.out.print("Less than Q ");
							eventQueue.add(new Event(futureTime, "IO", currentBurst));
							currentProcess.setFirstBurst(generateCPUBurst(currentProcess.isCpuBound()));
							System.out.print(" | --> " + "IO at " + futureTime + " ");
						}
					}



				}else{
					if(readyQueue.isEmpty()){
						System.out.print("No Processes in Queue");
					}else{
						System.out.print("CPU is Busy");
					}
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
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				System.out.print(" | --> " + "IO Serve ");
				break;


			case "IO Serve":
				System.out.print("IO Serve | ");
				Process ioServeProcess = ioQueue.removeFirst();
				double  ioServiceTime = generateRandomTime(avgInterruptTime);
				ioServeProcess.setTimeInIO(ioServeProcess.getTimeInIO() + ioServiceTime);
				System.out.print("Running IO Service Time with " + ioServiceTime + " | " 
						+ "Total Time in IO " + ioServeProcess.getTimeInIO() + " | Process ID : " + ioServeProcess.getPid());
				double timeAfterProcessing = (ioServiceTime + current.getTime());
				updateIOServeTime(ioServeProcess, ioServiceTime);

				ioServQueue.add(ioServeProcess);
				eventQueue.add(new Event(timeAfterProcessing, "Back Ready"));
				System.out.print(" | --> " + "Back Ready ");
				break;

			case "Back Ready":
				System.out.print("Back Ready | ");
				Process readyProcess = ioServQueue.removeFirst();
				readyQueue.add(readyProcess);
				readyProcess.setTimeEnteredWaiting(current.getTime());
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				System.out.print(" | --> " + "Scheduler ");
				break;

			case "Quantum":
				System.out.print("Quantum | ");
				Process qProcess = cpuQueue.poll();
				System.out.println( "-Before :"+qProcess.getCpuTime() + " ");
				qProcess.setCpuTime(qProcess.getCpuTime() - q);

				//Count CPU Usage
				upDateCPUuse(qProcess, q);
				readyQueue.add(qProcess);
				qProcess.setTimeEnteredWaiting(current.getTime());
				System.out.println("-After :"+qProcess.getCpuTime() + "  ");
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				System.out.print(" | --> " + "Scheduler ");
				break;

			case "Done":
				System.out.print("Done | ");
				Process doneProcess = cpuQueue.poll(); 

				//Count CPU Usage
				upDateCPUuse(doneProcess, doneProcess.getCpuTime());
				addCompleted(doneProcess);
				addTurnArundTime(doneProcess, current);
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				System.out.print(" | --> " + "Scheduler ");
				break;

			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
		System.out.println("OVERALL");
		System.out.println("Simulation Time: " + df.format(totSimTime));
		System.out.println("Created " + (cpuProcessCount + ioProcessCount) + " processes");
		System.out.println("Average CPU Time: " + df.format(((double) (cpuCPUTime + ioCPUTime)/(cpucpuUseCount + iocpuUseCount))));
		System.out.println("CPU utilization: " + ((cpuCPUTime + ioCPUTime) / totSimTime) * 100 + "%");
		System.out.println("Total time in context switches: " + df.format(contextSwitchTime));
		System.out.println();


		System.out.println("TOTAL number of proc. completed: " + (ioBoundCount + cpuBoundCount));
		System.out.println("Ratio of I/O-bound completed: " );
		System.out.println("Average CPU Time :" + df.format(((double) (cpuCPUTime + ioCPUTime)/(ioBoundCount + cpuBoundCount))));
		System.out.println("Average ready waiting time: " + totalWaitTime / (cpuProcessCount + ioProcessCount));
		System.out.println("Average turnaround time: " + df.format(((double)(  (cpuTurnAroundTime + ioTurnAroundTime)/(cpuBoundCount + ioBoundCount) ))));
		System.out.println();


		System.out.println("Number of I/O-BOUND proc. completed: " + ioBoundCount);
		System.out.println("Average CPU time: " + df.format(((double)ioCPUTime/ioBoundCount)));
		System.out.println("Average ready waiting time: ");
		double count = 0.0;
		if(ioIOCount > 0)
			count = (double)(ioIOTime/ioBoundCount);
		System.out.println("Average I/O service time: " + df.format((count)));		
		System.out.println("Average turnaround time: " + df.format(((double)(ioTurnAroundTime/ioBoundCount))));
		System.out.println("Average I/O interrupts/proc.: " + (ioInterCount/ioBoundCount));
		System.out.println();


		System.out.println("Number of CPU-BOUND proc. completed: " + cpuBoundCount);
		System.out.println("Average CPU time: " + df.format(((double)cpuCPUTime/cpuBoundCount)));
		System.out.println("Average ready waiting time: ");
		count = 0.0;
		if(cpuIOCount > 0)
			count = (double)cpuIOTime/cpuBoundCount;
		System.out.println("Average I/O service time: " + df.format((count)));


		System.out.println("Average turnaround time: " + df.format(((double)(cpuTurnAroundTime/cpuBoundCount))));
		System.out.println("Average I/O interrupts/proc.: " + (cpuInterCount/cpuBoundCount));
		System.out.println();




		System.out.println("Total Sim Time is " + totSimTime);
		System.out.println("Quantum is " + q);
		System.out.println("Context Switch is " + conSwitch);
		System.out.println("Average Process Time " + avgProcessLength);
		System.out.println("Average Time Between Processes " + avgTimeBetweenProc);
		System.out.println("IO Percentage " + perJobIo);
		System.out.println("Average Interrupt Time " + avgInterruptTime);

	}

	public static void addProcessCount(Process p){
		if(p.isCpuBound())
			cpuProcessCount++;
		else
			ioProcessCount++;
	}

	public static void addTurnArundTime(Process p, Event e){
		if(p.isCpuBound()){
			cpuTurnAroundTime += e.getTime() - p.getTimeCreated();
		}
		else{
			ioTurnAroundTime += e.getTime() - p.getTimeCreated();
		}
	}

	public static void addInterCount(Process p){
		if(p.isCpuBound())
			cpuInterCount++;
		else
			ioInterCount++;
	}

	public static void updateIOServeTime(Process p, double timeUsed){
		if(p.isCpuBound()){
			cpuIOTime =  cpuIOTime + timeUsed;
			cpuIOCount++;
		}else{
			ioIOTime = ioIOTime + timeUsed;
			ioIOCount++;
		}

	}

	//Random Number Generated Based on Input
	public static double generateRandomTime(double avgTimeBetweenProc2){

		// take the natural log of (a random FLOAT between 0 and 1)
		// then multiply by the negative of the expected ("average") value passed in
		double nice = -avgTimeBetweenProc2 * Math.log ( Math.random() );
		// in my code, all numbers are in millionths of a second, but I round all random numbers
		// to the nearest 100 microseconds (chop off the last two digits)
		// here's a simple way to do that with a float or double:
		//	    nice *= 10000;
		//	    nice = (int) nice;
		//	    nice /= 10000;
		return ( nice );

		//		return (double) ((avgTimeBetweenProc2/2) + (Math.random() * avgTimeBetweenProc2));
	}

	//Percent CPU/IO Generator
	public static boolean generateIO(double percent){
		Random rand = new Random();
		double tester = rand.nextInt(101);
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
		if(process.isCpuBound()){
			cpucpuUseCount++;
			cpuCPUTime = cpuCPUTime + timeUsed;
			System.out.println("----------------------USED CPU TOTAL : " + cpuCPUTime);
		}else{
			iocpuUseCount++;
			ioCPUTime += timeUsed;
		}
	}

	public static double generateCPUBurst(boolean cpuBound){
		Random rand = new Random();
		if(cpuBound){
			double time = ((double)rand.nextInt((10001)) + 10000) / 1000000;
			System.out.println("----------------------CPU TIME GENERATED : " + time);
			return time;
		}
		else
			return  ((double)rand.nextInt((2001)) + 2000) / 1000000;
	}

	public static void addCompleted(Process p){
		if(p.isCpuBound()){
			cpuBoundCount++;
		}else{
			ioBoundCount++;
		}

	}
}
