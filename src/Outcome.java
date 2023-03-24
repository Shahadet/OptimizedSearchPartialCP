
public class Outcome {
	int[] values;
	public Outcome() {
		values = new int[SearchPartialCP.numberOfvariables]; 
	}
	public Outcome(int numberofVariables) {
		values = new int[numberofVariables]; 
	}
	public Outcome(Outcome c) {
		values = new int[c.values.length]; 
		for(int i=0;i<values.length;i++) {
			this.values[i] = c.values[i];
				
		}
	}
	
	public Outcome(int[] values) {
		this.values = new int[SearchPartialCP.numberOfvariables]; 
		for(int i=0;i<values.length;i++) {
			this.values[i] = values[i];
		}
	}
	public void set(int[] values) {
		for(int i=0;i<values.length;i++) {
			this.values[i] = values[i];
		}
	}
	public boolean equals(Outcome o2) {
		for(int i=0;i<values.length;i++) {
			if(this.values[i] != o2.values[i])
				return false;
		}
		return true;
	
	}
	public void combine(Outcome o2) {
		for(int i=0;i<values.length;i++) {
			if(o2.values[i]>0)
				this.values[i] = o2.values[i];
				
		}
	}
	public boolean isTotal() {
		for(int i=0;i<values.length;i++) {
			if(this.values[i] <=0 )
				return false;
		}
		return true;
	
	}
	@Override
	public String toString() {
		String s = "";
		for(int i = 0;i<values.length;i++)
			s= s+ values[i]+" ";
		return s;
	}
}