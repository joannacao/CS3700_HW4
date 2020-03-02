package reverseDeclaration;

import java.util.*; 
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock; 

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
	private static CyclicBarrier one = new CyclicBarrier(2);
	private static CyclicBarrier two = new CyclicBarrier(2);
	private static CyclicBarrier three = new CyclicBarrier(2);
	BufferedWriter writer;
	
	public PrintBackwards(ArrayList<String> s, int t, int n, BufferedWriter b) {
		this.sArray = s;
		this.numOfThreads = t;
		this.threadNum = n;
		this.writer = b;
	} 
	public void run(){
		if (this.threadNum == 2) {
			try {
				one.await(); 
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		} else if (this.threadNum == 3) {
			try {
				two.await(); 
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		} else if (this.threadNum == 4) {
			try {
				three.await(); 
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
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
			for (int i = yourPortionEnd; i >= yourPortionStart; i--) {
				lock1.lock();
				writer.write(sArray.get(i)); //write word to file
				writer.write(" ");
				lock1.unlock();
			}
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		if (this.threadNum == 1) {
			try {
				one.await(); 
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		} else if (this.threadNum == 2) {
			try {
				two.await(); 
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		} else if (this.threadNum == 3) {
			try {
				three.await(); 
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
	}
}

public class ReverseWord {
	
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
		FileWriter fw = new FileWriter("C:\\Users\\joann\\Downloads\\backwards.txt");
		BufferedWriter bw = new BufferedWriter(fw); 
		
		long startTime = System.currentTimeMillis(); 
		for (int i = 1; i <= 4; i++) {
			new Thread(new PrintBackwards(stringArray, i, 4, bw)).start(); //create four threads
		}
		long endTime = System.currentTimeMillis(); 
		long time = endTime - startTime; 
		System.out.println("Time to complete: " + time + " ms");
		scan.close();
	}

}
