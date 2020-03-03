package cs3700_game1;

import java.util.*;
import java.util.concurrent.*;

//what values does each player need to know?
//score arraylist
//hand arraylist
//player number
//total number (can probably just get from arraylist size)

//player thread now represents both winner and player. determine what logic to follow by name. 

class PlayerThread implements Runnable {
	private int playerNumber; //player id 
	private int totalPlayers; //total number of players
	private int playerHand; // 0 = rock, 1 = paper, 2 = scissors
	private ArrayList<Integer> allHands; //arraylist of every players hand. index represents id
	private ArrayList<Integer> allScores; //arraylist of every players score after competing w other players
	private ArrayList<Integer> losers; //0 at index id = still playing, -1 = lost
	static CyclicBarrier first;
	static CyclicBarrier second;
	static CyclicBarrier third;
	private int score;
	
	public PlayerThread(ArrayList<Integer> h, ArrayList<Integer> s, ArrayList<Integer> l, int num, int total) {
		this.playerNumber = num;
		this.allHands = h;
		this.allScores = s;
		this.totalPlayers = total;
		this.score = 0;
		this.losers = l;	
	}
	public void run() {
			//randomly select hand
			Random rand = new Random();
			this.playerHand = rand.nextInt(3); 
			this.allHands.set(this.playerNumber, this.playerHand);
			try {
				first.await(); //wait for everyone to obtain hand
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < this.totalPlayers; i++) {
				if(this.losers.get(i) == 0 && this.allHands.get(i) != null) { //while still in game and hand isn't null
					//iterate through all players and calculate this player/thread's score
					if (this.playerHand == 0) {
						if (this.allHands.get(i) == 1) {
							this.score--;
						} else if (this.allHands.get(i) == 2) {
							this.score++;
						}
					} else if (this.playerHand == 1) {
						if (this.allHands.get(i) == 0) {
							this.score++;
						} else if (this.allHands.get(i) == 3) {
							this.score--;
						}
					} else if (this.playerHand == 2) {
						if (this.allHands.get(i) == 0) {
							this.score--;
						} else if (this.allHands.get(i) == 1) {
							this.score++;
						}
					}
				}
			}
			this.allScores.set(this.playerNumber, this.score); //add score at player's index 
			try {
				second.await(); //wait for rest of players to finish
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
			try {
				third.await(); //waits for winner thread
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
	}
}

//what values does winner thread need to know? 
//score arraylist 

class WinnerThread implements Runnable {
	private ArrayList<Integer> allScores; //arraylist of all players score 
	private ArrayList<Integer> losers; //arraylist of losers 
	private int numPlayers;
	private ArrayList<Integer> allHands; 
	private boolean lastTwo; //boolean to see if we're comparing the last two players 
	
	public WinnerThread(ArrayList<Integer> s, ArrayList<Integer> h, ArrayList<Integer> l, int n, boolean lt) {
		this.allScores = s;
		this.losers = l;
		this.numPlayers = n;
		this.allHands = h;
		this.lastTwo = lt;
	}
	public void run() {
		try {
			PlayerThread.second.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		//check scores 
		//check if there's only two players left 
		if (lastTwo) {
			//find winner and print
			int win = -1;
			int winNumber = -1;
			//first available player is initialized as winner
			while (win == -1) {
				int index = 0; 
				if (this.allScores.get(index) != null) {
					win = this.allScores.get(index);
					winNumber = index;
				}
				index++;
			}
			//compare every available players scores to find highest score
			for (int i = winNumber; i < this.allScores.size(); i++) {
				if (this.allScores.get(i) != null) {
					if (win <= this.allScores.get(i)) {
						win = this.allScores.get(i); 
						winNumber = i;
					}
				}
			}
			System.out.println("Winner is Player " + winNumber);
		} else { //if more than two players, find loser instead of winner
			int low = this.numPlayers*2;
			int loserNumber = -1;
			//first available player is initialized as loser
			while (loserNumber == -1) {
				int index = 0;
				if (this.allScores.get(index) != null) {
					low = this.allScores.get(index);
					loserNumber = index;
				}
				index++;
			}
			//compare score to find loser 
			for (int i = loserNumber; i < this.allScores.size(); i++) {
				if (this.allScores.get(i) != null) {
					if (low > this.allScores.get(i)) {
						low = this.allScores.get(i);
						loserNumber = i;
					}
				}
			} 
			//make score and hand null
			this.allScores.set(loserNumber, null);
			this.allHands.set(loserNumber, null);
			//change players value in loser arraylist to -1
			this.losers.set(loserNumber, -1);
		}
		try {
			PlayerThread.third.await(); //terminates all threads once this thread is done
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
}

public class Game1 {
	
	public static void main(String[] args) {
		System.out.println("Enter number of players: ");
		Scanner scan = new Scanner(System.in);
		int numPlayers = scan.nextInt();
		//arraylist for hand and score 
		ArrayList<Integer> hand = new ArrayList<Integer>(numPlayers);
		ArrayList<Integer> score = new ArrayList<Integer>(numPlayers);
		//arraylist to keep track of losers 
		ArrayList<Integer> losers = new ArrayList<Integer>(numPlayers);
		for (int i = 0; i < numPlayers; i++) {
			hand.add(null);
		}
		for (int i = 0; i < numPlayers; i++) {
			score.add(null);
		}
		for (int i = 0; i < numPlayers; i++) {
			losers.add(0);
		}
		long startTime = System.currentTimeMillis();
		for (int i = numPlayers; i > 1; i--) {
			PlayerThread.first = new CyclicBarrier(i);
			PlayerThread.second = new CyclicBarrier(i+1);
			PlayerThread.third = new CyclicBarrier(i+1);
			for (int x = 0; x < i; x++) {
				if (losers.get(x) == 0) {
					new Thread(new PlayerThread(hand, score, losers, x, numPlayers)).start();
				}
			}
			//start WinnerThread
			if (i == 2) {
				new Thread(new WinnerThread(score, hand, losers, numPlayers,true)).start();
			} else {
				new Thread(new WinnerThread(score, hand, losers, numPlayers,false)).start();
			}
		}
		long endTime = System.currentTimeMillis(); 
		long time = endTime - startTime; 
		System.out.println("Time to execute game: " + time + " ms"); 
		scan.close();
	}

}
