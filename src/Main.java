

public class Main{

	public static void main(String[] args) {
		//CPNetGenerator.main(null);
		long startTime = System.nanoTime();
		SearchPartialCP.useDominanceTesting = true;
		SearchPartialCP.main(null);
		
		long endTime = System.nanoTime();
		// get difference of two nanoTime values
		long timeElapsed = endTime - startTime;
		System.out.println("Execution time in milliseconds : " + timeElapsed / 1000000);
	}
}


