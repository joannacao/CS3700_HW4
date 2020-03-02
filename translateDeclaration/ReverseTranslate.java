package translateDeclaration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;

class PrintBackwards implements Runnable {
	//what information do we need?
	//string array
	private ArrayList<String> sArray;
	//need to know total number of threads? so we can try to divide work 
	private int numOfThreads;
	//what number thread you identify as so you know what section to do 
	private int threadNum; 
	private static Lock lock1 = new ReentrantLock(); 
	//create 3 barriers for 4 threads
	static CyclicBarrier one;
	//private static CyclicBarrier two = new CyclicBarrier(2);
	//private static CyclicBarrier three = new CyclicBarrier(2);
	BufferedWriter writer;
	
	public PrintBackwards(ArrayList<String> s, int t, int n, BufferedWriter b) {
		this.sArray = s;
		this.numOfThreads = t;
		this.threadNum = n;
		this.writer = b;
	} 
	
	 public static void translateText(String projectId, String targetLanguage, String text, ArrayList<String> sa, int index)
		      throws IOException {
		 	//taken from google cloud github 
		    try (TranslationServiceClient client = TranslationServiceClient.create()) {
		      LocationName parent = LocationName.of(projectId, "global");

		      TranslateTextRequest request =
		          TranslateTextRequest.newBuilder()
		              .setParent(parent.toString())
		              .setMimeType("text/plain")
		              .setTargetLanguageCode(targetLanguage)
		              .addContents(text)
		              .build();

		      TranslateTextResponse response = client.translateText(request);

		      // Display the translation for each input text provided
		      for (Translation translation : response.getTranslationsList()) {
		    	  sa.set(index, translation.getTranslatedText()); //reset words in sArray as translated version
		      }
		    }
		  }
	 
	public void run(){
		//determine target language
		String targetLanguage = "ja"; 
		String project_ID = "my-project-1582959714066";
		//divide up array by number of threads
		//determine how big each portion of writing is
		int fileSize = sArray.size(); 
		
		int writePortion = fileSize/this.numOfThreads; 
		int yourPortionStart = 0;
		int yourPortionEnd = writePortion - 1; 
		for (int i = 2; i <= numOfThreads; i++) {
			if (threadNum >= i) {
				yourPortionStart = yourPortionEnd + 1;
				yourPortionEnd += writePortion - 1;
			}
		} 
		try {
			for (int i = yourPortionStart; i < yourPortionEnd; i++) {
				translateText(project_ID, targetLanguage, sArray.get(i),sArray, i);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//use this info to have thread send chunk of array to google server and replace value in array
		//create barrier to wait 
		try {
			one.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		//create filewriter and bufferedwriter
		try {
			for (int i = sArray.size()-1; i >= 0; i--) {
				lock1.lock();
				writer.write(sArray.get(i));
				writer.write(" ");
				lock1.unlock();
			}
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}
}

public class ReverseTranslate {
	public static void main(String[] args) throws Exception{
		File file = new File("C:\\Users\\joann\\Downloads\\DeclatationIndependence.txt");
		Scanner scan = new Scanner(file);
		//store in a arraylist of strings. each element is one word. 
		ArrayList<String> stringArray = new ArrayList<String>(2000);
		while (scan.hasNextLine()) {
			Scanner wordScan = new Scanner(scan.nextLine());
			while (wordScan.hasNext()) {
				String s = wordScan.next();
				//store in arraylist 
				stringArray.add(s); 
			}
			wordScan.close();
		}
		FileWriter fw = new FileWriter("C:\\Users\\joann\\Downloads\\backwardsTranslated.txt");
		BufferedWriter bw = new BufferedWriter(fw); 
		
		PrintBackwards.one = new CyclicBarrier(4);
		long startTime = System.currentTimeMillis(); 
		for (int i = 1; i <= 4; i++) {
			new Thread(new PrintBackwards(stringArray, i, 4, bw)).start();
		}
		long endTime = System.currentTimeMillis(); 
		long time = endTime - startTime; 
		System.out.println("Time to complete: " + time + " ms");
		scan.close();
	}
}
