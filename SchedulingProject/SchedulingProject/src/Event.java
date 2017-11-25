import java.util.Comparator;

public class Event implements Comparable<Event> {
	
	private double time;
	private String type;
	private double burstTimeUsed;
	
	public double getBurstTimeUsed() {
		return burstTimeUsed;
	}

	public void setBurstTimeUsed(double burstTimeUsed) {
		this.burstTimeUsed = burstTimeUsed;
	}

	public Event(double d, String type) {
		super();
		this.time = d;
		this.type = type;
	}
	
	public Event(double timeAfterProcessing, String type, double burstTime) {
		super();
		this.burstTimeUsed = burstTime;
		this.time = timeAfterProcessing;
		this.type = type;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String toString(){
		return this.getType() + " " + this.getTime();
	}

	@Override
	public int compareTo(Event o) {
		if(o.getTime() > this.getTime())
			return -1;
		else
			return 1;
	}


}
