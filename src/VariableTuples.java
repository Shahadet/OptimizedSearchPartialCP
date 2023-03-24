import java.util.Objects;

//---------------------------------------------------------Hard Constraints Structure--------------------------------//
//hard_constraints = A,B: 1 1 ,  1  2  ;  B,A : 1 1 , 1 2 
public class VariableTuples{
	char a;
	char b;
	int aInt;
	int bInt;
	public VariableTuples(char a,char b) {
		this.a=a;
		this.b=b;
		this.aInt = a-'A'+1;
		this.bInt = b-'A'+1;
	}
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableTuples tuple = (VariableTuples) o;
        return (a == tuple.a && b == tuple.b);
    }
	@Override
    public int hashCode() {
        return Objects.hash(a+","+b);
    }
}