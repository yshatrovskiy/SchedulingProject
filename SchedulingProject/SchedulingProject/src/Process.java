import java.util.Random;

public class Process {
	
	long timeCreated;
	long runningTime;
	long cpuTime;
	long pid;
	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	boolean cpuBound;
	
	public Process(long pid, long timeCreated, long d, boolean cpuBound) {
		super();
		this.timeCreated = timeCreated;
		this.cpuTime = d;
		this.cpuBound = cpuBound;
		this.pid = pid;
	}

	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(int timeCreated) {
		this.timeCreated = timeCreated;
	}

	public long getCpuTime() {
		return cpuTime;
	}

	public void setCpuTime(long l) {
		this.cpuTime = l;
	}

	public boolean isCpuBound() {
		return cpuBound;
	}

	public void setCpuBound(boolean cpuBound) {
		this.cpuBound = cpuBound;
	}
	
	public long getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(int runningTime) {
		this.runningTime = runningTime;
	}

	

}
