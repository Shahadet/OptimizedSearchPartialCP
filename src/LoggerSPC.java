import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LoggerSPC {
	private String logFileName="src/SPC.log";
	private FileWriter myWriter = null;
	BufferedWriter bw;
	public LoggerSPC() {
		try {
			File fout = new File(logFileName);
			FileOutputStream  fos = new FileOutputStream(fout);
			bw = new BufferedWriter(new OutputStreamWriter(fos));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void info(String log) {
		 try {
			bw.write(log);
			bw.newLine();
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		LoggerSPC spc = new LoggerSPC();
		spc.info("Test");
	}
}
