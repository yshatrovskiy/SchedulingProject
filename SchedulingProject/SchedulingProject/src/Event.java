import java.util.Comparator;

public class Event implements Comparable<Event> {
	
	private long time;
	private String type;
	private long burstTimeUsed;
	
	public long getBurstTimeUsed() {
		return burstTimeUsed;
	}

	public void setBurstTimeUsed(long burstTimeUsed) {
		this.burstTimeUsed = burstTimeUsed;
	}

	public Event(long timeAfterProcessing, String type) {
		super();
		this.time = timeAfterProcessing;
		this.type = type;
	}
	
	public Event(long timeAfterProcessing, String type, long burstTime) {
		super();
		this.burstTimeUsed = burstTime;
		this.time = timeAfterProcessing;
		this.type = type;
	}

	public long getTime() {
		return time;
	}

	public void setTime(int time) {
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
