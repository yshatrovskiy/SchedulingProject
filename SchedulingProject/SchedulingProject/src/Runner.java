import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;


// Yevgeniy Shatrovskiy, Brandon Deluca, Ivan Tang
//  This class is the main loop and will do all of the logic.  The other two files are simply
//  classes for event and will only hold certain information.
//  --------------If you would like to see the event queue at any time, just uncomment line number 68

public class Runner {

	static double clock= 0;

	static LinkedList<Integer> inputValues = new LinkedList<Integer>();
	static LinkedList<Process> readyQueue = new LinkedList<Process>();
	static PriorityQueue<Process> cpuQueue = new PriorityQueue<Process>(1);
	static LinkedList<Process> ioQueue = new LinkedList<Process>();
	static LinkedList<Process> ioServQueue = new LinkedList<Process>();
	static PriorityQueue<Event> eventQueue = new PriorityQueue<Event>(20);

	static double 
	cpuProcessCount = 0, ioProcessCount = 0, q, conSwitch, avgProcessLength, 
	avgTimeBetweenProc, perJobIo, avgInterruptTime, totSimTime, 
	contextSwitchTime,ioCPUTime = 0,cpuCPUTime = 0,cpuIOTime = 0,
	ioIOTime = 0,	cpuBoundCount = 0, ioBoundCount = 0, iocpuUseCount = 0,
	cpucpuUseCount = 0,cpuIOCount = 0,ioIOCount = 0,ioTurnAroundTime = 0,
	cpuTurnAroundTime = 0,ioInterCount,cpuInterCount,ioWaitTime,cpuWaitTime;

	public static void main(String args[]) throws FileNotFoundException{


		BufferedReader brTest;
		try {
			String line;
			brTest = new BufferedReader(new FileReader("input_params_1.txt"));
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

		//Reverse Symbol for clock to work
		while(clock < totSimTime){
//			printEvents(eventQueue);


			Event current = eventQueue.poll();	

			//Set Time
			clock = current.getTime();
			switch(current.getType()){

			//Create a new process, add event to create new 
			//process with time stamp of clock + average time between processes
			case "New":

				pid++;
				//First Occurrence needs a special case
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
				break;

			case "Scheduler":
				
				//Pop eventQueue and put process on CPU
				//If not empty, event does nothing
				if(!readyQueue.isEmpty() && cpuQueue.isEmpty()){
					Process currentProcess = readyQueue.removeFirst();
					addWaitTime(currentProcess ,current.getTime() - currentProcess.getTimeEnteredWaiting());
					currentProcess.setTimeEnteredWaiting(0);
					cpuQueue.add(currentProcess);

					//If CPU bound, check CPUtime against quantum
					//Check cpuBurst first
					double currentBurst = currentProcess.getFirstBurst();
					double contextSwitch = generateRandomTime(conSwitch);
					contextSwitchTime = contextSwitchTime + contextSwitch;

					if(currentBurst > currentProcess.getCpuTime()  && q > currentProcess.getCpuTime()){
						//Call Done
						double futureTime = (double) (clock + currentProcess.getCpuTime() + contextSwitch);
						eventQueue.add(new Event(futureTime, "Done"));

					}else{

						if(currentBurst > q){
							//Call Quantum
							double futureTime = (double) (clock + q + contextSwitch);
							eventQueue.add(new Event(futureTime, "Quantum"));
							currentProcess.setFirstBurst(currentProcess.getFirstBurst()-q);
						}else{
							//Call IO
							addInterCount(currentProcess);
							double futureTime = (double) (clock + currentBurst + contextSwitch);
							eventQueue.add(new Event(futureTime, "IO", currentBurst));
							currentProcess.setFirstBurst(generateCPUBurst(currentProcess.isCpuBound()));
						}
					}
				}
				break;

	//Different case statement depending on what the event is	
			case "IO":
				Process ioProcess = cpuQueue.poll();
				ioProcess.setCpuTime(ioProcess.getCpuTime() - current.getBurstTimeUsed());

				//Count CPU Usage
				upDateCPUuse(ioProcess, current.getBurstTimeUsed());

				ioQueue.add(ioProcess);
				eventQueue.add(new Event(current.getTime(), "IO Serve"));
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				break;


			case "IO Serve":
				Process ioServeProcess = ioQueue.removeFirst();
				double  ioServiceTime = generateRandomTime(avgInterruptTime);
				ioServeProcess.setTimeInIO(ioServeProcess.getTimeInIO() + ioServiceTime);
				double timeAfterProcessing = (ioServiceTime + current.getTime());
				updateIOServeTime(ioServeProcess, ioServiceTime);

				ioServQueue.add(ioServeProcess);
				eventQueue.add(new Event(timeAfterProcessing, "Back Ready"));
				break;

			case "Back Ready":
				Process readyProcess = ioServQueue.removeFirst();
				readyQueue.add(readyProcess);
				readyProcess.setTimeEnteredWaiting(current.getTime());
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				break;

			case "Quantum":
				Process qProcess = cpuQueue.poll();
				qProcess.setCpuTime(qProcess.getCpuTime() - q);

				//Count CPU Usage
				upDateCPUuse(qProcess, q);
				readyQueue.add(qProcess);
				qProcess.setTimeEnteredWaiting(current.getTime());
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				break;

			case "Done":
				Process doneProcess = cpuQueue.poll(); 

				//Count CPU Usage
				upDateCPUuse(doneProcess, doneProcess.getCpuTime());
				addCompleted(doneProcess);
				addTurnArundTime(doneProcess, current);
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				break;

			}
		}
		
		//Create I/O, location of file
		OutputStream output = new FileOutputStream("output_Params1.txt");
		PrintStream printStream = new PrintStream(output);
		
		DecimalFormat tf = new DecimalFormat("#0.000");
		DecimalFormat cf = new DecimalFormat("00.00");
		DecimalFormat pf = new DecimalFormat("00.0");
		
		
		//Begin Printing To File
		double count = 0.0;
		printStream.println("Total Sim Time is " + totSimTime);
		printStream.println("Quantum is " + q);
		printStream.println("Context Switch is " + conSwitch);
		printStream.println("Average Process Time " + avgProcessLength);
		printStream.println("Average Time Between Processes " + avgTimeBetweenProc);
		printStream.println("IO Percentage " + perJobIo);
		printStream.println("Average Interrupt Time " + avgInterruptTime);
		printStream.println();
		printStream.println();
		printStream.println("OVERALL");
		printStream.println("Simulation Time: 						" + tf.format(totSimTime));
		printStream.println("Created " + (cpuProcessCount + ioProcessCount) + " processes");
		if((cpuProcessCount + ioProcessCount) > 0)
			count = ((double) (cpuCPUTime + ioCPUTime)/(cpuProcessCount + ioProcessCount));
		printStream.println("Average CPU Time: 						" + tf.format(count));
		printStream.println("CPU utilization: 						" + cf.format(((cpuCPUTime + ioCPUTime) / totSimTime) * 100) + "%");
		printStream.println("Total time in context switches: 		" + tf.format(contextSwitchTime));
		printStream.println();
		printStream.println("TOTAL number of proc. completed: 		" + (int)(ioBoundCount + cpuBoundCount));
		printStream.println("Ratio of I/O-bound completed: 			" + pf.format((ioBoundCount) / (ioBoundCount + cpuBoundCount)  * 100 ) + "%");
		count = 0.0;
		if((ioBoundCount + cpuBoundCount) > 0)
			count = ((double) (cpuCPUTime + ioCPUTime)/(ioBoundCount + cpuBoundCount));
		printStream.println("Average CPU Time :						" + tf.format(count) + " Second");
		printStream.println("Average ready waiting time: 			" + tf.format((ioWaitTime + cpuWaitTime)/ (cpuProcessCount + ioProcessCount)) + " Seconds");
		printStream.println("Average turnaround time: 				" + tf.format(((double)(  (cpuTurnAroundTime + ioTurnAroundTime)/(cpuBoundCount + ioBoundCount) ))) + " Seconds");
		printStream.println();
		printStream.println("Number of I/O-BOUND proc. completed: 	" + (int)ioBoundCount);	
		count = 0.0;
		if(ioBoundCount > 0)
			count = ((double)ioCPUTime/ioBoundCount);
		printStream.println("Average CPU time: 						" + tf.format(count) + " Seconds");
		printStream.println("Average ready waiting time: 			" + tf.format(ioWaitTime/cpuBoundCount) + " Seconds");
		count = 0;
		if(ioIOCount > 0)
			count = (double)(ioIOTime/ioBoundCount);
		printStream.println("Average I/O service time: 				" + tf.format((count)) + " Seconds");
		count = 0;
		if(ioBoundCount > 0)
			count = ((double)(ioTurnAroundTime/ioBoundCount));
		printStream.println("Average turnaround time: 				" + tf.format(count) + " Seconds");
		count = 0;
		if(ioBoundCount > 0)
			count = (ioInterCount/ioBoundCount);
		printStream.println("Average I/O interrupts/proc.: 			" + cf.format(count));
		printStream.println();
		printStream.println("Number of CPU-BOUND proc. completed: 	" + (int)cpuBoundCount);
		printStream.println("Average CPU time: 						" + tf.format(((double)cpuCPUTime/cpuBoundCount)) + " Seconds");
		printStream.println("Average ready waiting time: 			" + tf.format(cpuWaitTime/cpuBoundCount) + " Seconds");
		count = 0.0;
		if(cpuIOCount > 0)
			count = (double)cpuIOTime/cpuBoundCount;
		printStream.println("Average I/O service time: 				" + tf.format((count)) + " Seconds");
		printStream.println("Average turnaround time: 				" + tf.format(((double)(cpuTurnAroundTime/cpuBoundCount))) + " Seconds");
		printStream.println("Average I/O interrupts/proc.: 			" + cf.format((cpuInterCount/cpuBoundCount)));
		printStream.println();

	}
	
	public static void addWaitTime(Process p, double time){
		if(p.isCpuBound())
			cpuWaitTime += time;
		else
			ioWaitTime += time;
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
		double nice = -avgTimeBetweenProc2 * Math.log ( Math.random() );
		return ( nice );
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

	//Optional print events method that will print all events in Queue
	public static void printEvents(PriorityQueue<Event> eventQueue){
		System.out.println();
		System.out.println("--Event Queue--");
		for (Event event: eventQueue){
			System.out.print(event.toString() + " | ");
		}
		System.out.println();
	}

	//Update CPU use based on type and time used
	public static void upDateCPUuse(Process process, double timeUsed){
		if(process.isCpuBound()){
			cpucpuUseCount++;
			cpuCPUTime = cpuCPUTime + timeUsed;
		}else{
			iocpuUseCount++;
			ioCPUTime += timeUsed;
		}
	}

	//Burst generator, dependent on whether is CPU or IO Bound
	public static double generateCPUBurst(boolean cpuBound){
		Random rand = new Random();
		if(cpuBound){
			double time = ((double)rand.nextInt((10001)) + 10000) / 1000000;
			return time;
		}
		else
			return  ((double)rand.nextInt((2001)) + 2000) / 1000000;
	}

	//Increase completed process count based on type
	public static void addCompleted(Process p){
		if(p.isCpuBound()){
			cpuBoundCount++;
		}else{
			ioBoundCount++;
		}

	}
}
