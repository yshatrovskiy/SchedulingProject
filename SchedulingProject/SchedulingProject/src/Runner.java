import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;


public class Runner {


	static long clock= 0;

	static LinkedList<Integer> inputValues = new LinkedList<Integer>();
	static LinkedList<Process> readyQueue = new LinkedList<Process>();
	static PriorityQueue<Process> cpuQueue = new PriorityQueue<Process>(1);
	static LinkedList<Process> ioQueue = new LinkedList<Process>();
	static LinkedList<Process> ioServQueue = new LinkedList<Process>();
	static PriorityQueue<Event> eventQueue = new PriorityQueue<Event>(20);

	static long cpuProcessCount, ioprocessCount, q, 
	conSwitch, avgProcessLength, avgTimeBetweenProc, 
	perJobIo, avgInterruptTime;

	static long totSimTime;

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

			q = inputValues.get(1);

			conSwitch = inputValues.get(2);

			avgProcessLength = inputValues.get(3);

			avgTimeBetweenProc = inputValues.get(4);

			perJobIo = inputValues.get(5);

			avgInterruptTime = inputValues.get(6);


			//			
			System.out.println(totSimTime);
			System.out.println(q);
			//			System.out.println(conSwitch);
			//			System.out.println(avgProcessLength);
						System.out.println(avgTimeBetweenProc);
						System.out.println(perJobIo);
			//			System.out.println(avgInterruptTime);


		} catch (IOException e1) {
			e1.printStackTrace();
		}


		boolean firstOccurance = true;
		eventQueue.add(new Event(0, "New"));
		int pid = 0;
		long currentBurst;
		//Reverse Symbol for clock to work
		while(clock < totSimTime){

			
			printEvents(eventQueue);
			Event current = eventQueue.poll();	

			//Set Time
			clock = current.getTime();

			switch(current.getType()){

			//Create a new process, add event to create new 
			//process with time stamp of clock + average time between processes
			case "New":

				System.out.print("New Event");
				pid++;
				if(firstOccurance){
					readyQueue.add(new Process(pid, 0, (long)generateRandomTime(avgProcessLength), generateIO(perJobIo)));
					firstOccurance = false;
				}else{
					readyQueue.add(new Process(pid, current.getTime(), generateRandomTime(avgProcessLength), generateIO(perJobIo)));
				}

				//Add event to create new process and event for scheduler
				eventQueue.add(new Event(clock + generateRandomTime(avgTimeBetweenProc), "New"));
				eventQueue.add(new Event(clock, "Scheduler"));
				break;

			case "Scheduler":
				System.out.println("Scheduler Event:");

				//Pop eventQueue and put process on CPU
				//If not empty, event does nothing
				if(!readyQueue.isEmpty() && cpuQueue.isEmpty()){
					Process currentProcess = readyQueue.removeFirst();
					cpuQueue.add(currentProcess);
					System.out.println(" - " + " is CPU Bound: " +currentProcess.isCpuBound() + " | Time Remaining: " + currentProcess.getCpuTime() + " | ID: " +currentProcess.getPid());


					if(currentProcess.getCpuTime() < q){

						//Call Done Event with new time
						int futureTime = (int) (clock + currentProcess.getCpuTime() + generateRandomTime(conSwitch));
						System.out.println(" -- CPU Time Less " + " Future Time: " + futureTime);
						eventQueue.add(new Event(futureTime, "Done"));

					}else{

						if(generateCPUBurst(currentProcess.isCpuBound()) > q){
							
							//Call Quantum event with new time
							int futureTime = (int) (clock + q + generateRandomTime(conSwitch));
							System.out.println(" -- CPU Bound " + " Future Time: " + futureTime);
							eventQueue.add(new Event(futureTime, "Quantum"));

						}else{
							
							//Call IO Event with new time
							long burst = generateCPUBurst(currentProcess.isCpuBound());
							int futureTime = (int) (clock + burst + generateRandomTime(conSwitch));
							System.out.println(" -- IO Bound " + " Future Time: " + futureTime + " with CPU Burst of " + burst);
							eventQueue.add(new Event(futureTime, "IO", burst));

						}
					}
				}else{
					System.out.println("No Processes in Queue");
				}
				


				break;

			case "IO":
				System.out.println("IO Event");
				Process ioProcess = cpuQueue.poll();
				ioProcess.setCpuTime(ioProcess.getCpuTime() - current.getBurstTimeUsed());
				ioQueue.add(ioProcess);
				eventQueue.add(new Event(current.getTime(), "IO Serve"));
				break;


			case "IO Serve":
				System.out.println("IO Serve Event");
				Process ioServeProcess = ioQueue.removeFirst();
				long timeAfterProcessing = (generateRandomTime(avgInterruptTime) + current.getTime());
				System.out.println(timeAfterProcessing);
				ioServQueue.add(ioServeProcess);
				eventQueue.add(new Event(timeAfterProcessing, "Back Ready"));
				break;

			case "Back Ready":
				System.out.println("Back Ready Event");
				Process readyProcess = ioServQueue.removeFirst();
				readyQueue.add(readyProcess);
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				break;

			case "Quantum":
				System.out.println("Quantum Event");
				Process qProcess = cpuQueue.poll();
				System.out.println( "-Before :"+qProcess.getCpuTime() + " ");
				qProcess.setCpuTime(qProcess.getCpuTime() - q);
				System.out.println("-After :"+qProcess.getCpuTime() + "  ");
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				break;

			case "Done":
				System.out.println("Done Event");
				Process doneProcess = cpuQueue.poll(); 
				doneProcess.setCpuTime(0);
				eventQueue.add(new Event(current.getTime(), "Scheduler"));
				break;

			}
			System.out.println();
			System.out.println("clock is " + clock + " Total Sim Time is : " + totSimTime);
		}
	}

	//Random Number Generated Based on Input
	public static long generateRandomTime(long avgTimeBetweenProc2){
		return (long) ((avgTimeBetweenProc2/2) + (Math.random() * avgTimeBetweenProc2));
	}

	//Percent CPU/IO Generator
	public static boolean generateIO(long percent){
		Random rand = new Random();
		long tester = rand.nextInt(101);
//		System.out.println();
//		System.out.println("Percent Entered: " + percent + " Result Number: " + tester);
		if(tester > percent)
			return true;
		else
			return false;
	}

	public static void printEvents(PriorityQueue<Event> eventQueue){
		System.out.println();
		System.out.println("--Events--");
		for (Event event: eventQueue){
			System.out.print(event.toString() + " | ");
		}
		System.out.println();
		System.out.println("--Events--");
		System.out.println();
	}
	
	public static int generateCPUBurst(boolean cpuBound){
		Random rand = new Random();
		if(cpuBound)
			return  rand.nextInt((10001) + 10000);
		else
			return  rand.nextInt((2001) + 2000);
	}
}
