import java.util.Objects;

//"["3", "4"], ["2", "3"], ["2", "1"]" the=[]0
public class Tuples{
	int a;
	int b;
	public Tuples(int a,int b) {
		this.a=a;
		this.b=b;
	}
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuples tuple = (Tuples) o;
        return (a == tuple.a && b == tuple.b);
    }
	@Override
    public int hashCode() {
        return Objects.hash(a+","+b);
    }
}