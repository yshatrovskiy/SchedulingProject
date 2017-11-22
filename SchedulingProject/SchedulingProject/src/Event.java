import java.util.Comparator;

public class Event implements Comparable<Event> {
	
	private int time;
	private String type;
	
	public Event(int time, String type) {
		super();
		this.time = time;
		this.type = type;
	}

	public int getTime() {
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

	@Override
	public int compareTo(Event o) {
		if(o.getTime() > this.getTime())
			return -1;
		else
			return 1;
	}


}
