import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class test {

	public static void main(String []args) {
		
		System.out.println(((char)('A'+1)+""));
		System.out.println('A'-'A'+1);
		
		Map<Integer,List<Integer>> domains;
		
		domains = new HashMap<Integer, List<Integer>>();
		for(int i =1;i<=6;i++) {
			List<Integer> domain= new ArrayList<Integer>();
			for(int j=1;j<=5;j++) {
				domain.add(j);
			}
			domains.put(i, domain);
		}
		Set<Integer> mapK= domains.keySet();
		for(Integer i:mapK) {
			if(i==2) {
				List<Integer> li = domains.get(2);
				List<Integer> li2 = new ArrayList<Integer>();
				for(Integer i2:li) {
					if(i2!=3)
						li2.add(i2);
				}
				domains.put(i, li2);
			}
		}
		domains.get(3).remove((Object)4);
		mapK= domains.keySet();
		for(Integer i:mapK) {
			List<Integer> li = domains.get(i);
			for(Integer i2:li) {
				System.out.print(i2);
				
			}
			System.out.println();
		}
		/*
		Set<Integer> domainSetP = new HashSet<Integer>();
		for(int i =1;i<=5;i++)	
			domainSetP.add(i);
		
		while(domainSetP.size()>0) {
			
		}*/
		int n=4;
		double p=0.33;
		double a=0.8;
		double r=0.7;
		double o=0.5;
		int maxParents=5;
		boolean fixParents=true;
		System.out.println("Num of v:"+n +" P="+p+" r="+r+" alpha="+a);
		
		double domainSize = (int) Math.round(Math.pow((double)n, a));
		//domainSize = 5;
		System.out.println("Domain Size ="+domainSize);

		int constraints = (int)Math.round(r*n* Math.log(n));
		System.out.println("Number Of Constraints ="+constraints);
		
		int noOfTuples = (int)Math.round(p*domainSize*domainSize);
		System.out.println("Number of Incompatible Tuples ="+noOfTuples);
		
		int numberOfPreferenceOrderinCPT = (int)Math.round( ((float)domainSize * (domainSize-1) / 2.0) * o);
		System.out.println("Number of Preference order ="+numberOfPreferenceOrderinCPT);
		
		double phaseTransition = 1.0 - Math.exp(-1*(a/r));
		System.out.println("Phase Transition:"+phaseTransition);
	}
	
}
