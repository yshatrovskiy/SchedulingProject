
public class Process {
	
	int timeCreated;
	int runningTime;
	int cpuTime;
	boolean cpuBound;
	int firstCPUBurst;
	
	public Process(int timeCreated, int cpuTime, boolean cpuBound) {
		super();
		this.timeCreated = timeCreated;
		this.cpuTime = cpuTime;
		this.cpuBound = cpuBound;
		
		if(cpuBound){
			this.firstCPUBurst = (4000/2) + (int) (Math.random() * 4000);
		}else{
			this.firstCPUBurst = 20000/2 + (int) (Math.random() * 20000);
		}
	}

	public int getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(int timeCreated) {
		this.timeCreated = timeCreated;
	}

	public int getCpuTime() {
		return cpuTime;
	}

	public void setCpuTime(int cpuTime) {
		this.cpuTime = cpuTime;
	}

	public boolean isCpuBound() {
		return cpuBound;
	}

	public void setCpuBound(boolean cpuBound) {
		this.cpuBound = cpuBound;
	}

	public int getFirstCPUBurst() {
		return firstCPUBurst;
	}

	public void setFirstCPUBurst(int firstCPUBurst) {
		this.firstCPUBurst = firstCPUBurst;
	}
	
	public int getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(int runningTime) {
		this.runningTime = runningTime;
	}

	

}
