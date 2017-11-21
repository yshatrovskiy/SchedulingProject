
public class Event {
	
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

}
