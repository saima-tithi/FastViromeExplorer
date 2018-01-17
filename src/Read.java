public class Read implements Comparable<Read>{
	private int startPos;
	private int endPos;
	
	public Read() {
		
	}
	
	public Read(int startPos, int endPos) {
		this.startPos = startPos;
		this.endPos = endPos;
	}
	
	public int getStartPos() {
		return startPos;
	}
	
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}
	
	public int getEndPos() {
		return endPos;
	}
	
	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	@Override
	public int compareTo(Read o) {
		int diff = this.startPos - o.startPos;
		if (diff == 0) {
			return (this.endPos - o.endPos);
		}
		return diff;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Read) && (this.compareTo((Read)o) == 0);
	}
}
