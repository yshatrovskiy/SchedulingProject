import java.util.Random;

public class Process {
	
	double timeCreated;
	double runningTime;
	double cpuTime;
	double pid;
	double timeInIO;
	
	public double getTimeInIO() {
		return timeInIO;
	}

	public void setTimeInIO(double timeInIO) {
		this.timeInIO = timeInIO;
	}

	public double getPid() {
		return pid;
	}

	public void setPid(double pid) {
		this.pid = pid;
	}

	boolean cpuBound;
	
	public Process(double pid, double timeCreated, double d, boolean cpuBound) {
		super();
		this.timeCreated = timeCreated;
		this.cpuTime = d;
		this.cpuBound = cpuBound;
		this.pid = pid;
	}

	public double getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(int timeCreated) {
		this.timeCreated = timeCreated;
	}

	public double getCpuTime() {
		return cpuTime;
	}

	public void setCpuTime(double l) {
		this.cpuTime = l;
	}

	public boolean isCpuBound() {
		return cpuBound;
	}

	public void setCpuBound(boolean cpuBound) {
		this.cpuBound = cpuBound;
	}
	
	public double getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(int runningTime) {
		this.runningTime = runningTime;
	}

	

}
