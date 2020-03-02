package cs3700_game2;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

class PlayerThread implements Runnable {
	private int playerNumber; 
	private ArrayList<Integer> hands; 
	private int choice;
	private static CyclicBarrier one = new CyclicBarrier(1);
	private static int playersLeft; //keeps track of the number of players left 
	private static CyclicBarrier tie = new CyclicBarrier(2);
	private ArrayList<Integer> taken = new ArrayList<Integer>(); //helps to pair up players, taking note of players already in pairs
	private static Lock lock1 = new ReentrantLock(); 
	
	public PlayerThread(int pn, ArrayList<Integer> h, ArrayList<Integer> t) {
		this.playerNumber = pn;
		this.hands = h;
		this.choice = -1;
		playersLeft = h.size();
		this.taken = t; 
	}
	public void run() {
		while (playersLeft >= 2) { 
			if (one.getParties() != playersLeft) {
				one = new CyclicBarrier(playersLeft);
			}
			//randomize throw
			Random rand = new Random(); 
			this.choice = rand.nextInt(3); 
			hands.set(this.playerNumber, this.choice);
			//barrier 
			try {
				one.await(); //wait for everyone to get hand
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
			//random pair up
			//if already paired, find pair
			int competitor = this.playerNumber; 
			lock1.lock(); //makes sure only one thread writes to taken at one time 
			if (taken.get(this.playerNumber)!=0) {
				competitor = taken.get(this.playerNumber);
				taken.set(competitor, this.playerNumber); 
			} else {
				while (competitor == this.playerNumber || hands.get(competitor) == -1 || taken.get(competitor) != 0) {
					competitor = rand.nextInt(hands.size()); 
					if (taken.get(competitor) == 0) {
						taken.set(this.playerNumber, competitor); 
					}
				} 
			}
			lock1.unlock(); 
			//check that player's hand
			//first check for tie 
			while (this.choice == hands.get(competitor)) {
				this.choice = rand.nextInt(3); 
				hands.set(this.playerNumber, this.choice);
				try {
					tie.await(); 
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace(); 
				}
			}
			//if you have rock
			if (this.choice == 0) {
				//if other gets paper
				if (hands.get(competitor) == 1) {
					//you lose
					//make hand null, break out of loop
					hands.set(this.playerNumber, -1); 
					break;
				} else if (hands.get(competitor) == 2) {
					//you win 
					//check if theres only two players left. if there is, print you won and break out of loop
					if (playersLeft == 2) {
						System.out.println("Player " + this.playerNumber + " won");
						break;
					}
					//loop back 
				}
			} else if (this.choice == 1) {
				if (hands.get(competitor) == 0) {
					//you win 
					if (playersLeft == 2) {
						System.out.println("Player " + this.playerNumber + " won");
						break;
					}
				} else if (hands.get(competitor) == 2) {
					//you lose
					hands.set(this.playerNumber,  -1);
					break;
				}
			} else if (this.choice == 2) {
				if (hands.get(competitor) == 0) {
					//you lose
					hands.set(this.playerNumber, -1); 
					break;
				} else if (hands.get(competitor) == 1) {
					//you win
					if (playersLeft == 2) {
						System.out.println("Player " + this.playerNumber + " won");
						break;
					}
				}
			}
			//if lose, break out of loop
			//divide playersLeft by half 
			playersLeft /= 2; 
		}

	}
}

public class Game2 {
	
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("How many players? Enter an even number: ");
		int numPlayers = scan.nextInt();
		
		//other info we need? 
		//arraylist of hands
		ArrayList<Integer> hands = new ArrayList<Integer>(numPlayers); 
		for (int i = 0; i < numPlayers; i++) {
			hands.add(-1);
		}
		ArrayList<Integer> take = new ArrayList<Integer>(numPlayers); 
		for (int i = 0; i < numPlayers; i++) {
			take.add(0);
		}
		
		//start playerthreads. no need for winnerthread anymore
		for (int i = 0; i < numPlayers; i++) {
			new Thread(new PlayerThread(i, hands, take)).start();
		}
		scan.close();
	}

}
