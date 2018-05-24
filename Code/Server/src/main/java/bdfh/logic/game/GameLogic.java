package bdfh.logic.game;

import bdfh.logic.Player;
import bdfh.logic.game.Card;
import bdfh.logic.game.Square;
import bdfh.net.server.ClientHandler;
import bdfh.logic.saloon.Lobby;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author Daniel Gonzalez Lopez
 * @author Bryan Curchod
 * @version 1.0
 */
public class GameLogic extends Thread {
	
	private static final int NB_DECKCARD = 20;
	private static final int NB_SQUARE = 40;
	private ArrayDeque<Player> joueurs;
	private ArrayDeque<Card> Deck;
	private Square[] board; // = new Square[NB_SQUARE];
	private int nbDice;
	
	/**
	 * constructor of a logic session. Define the turns, generate the board, and apply the parameters
	 * @param lobby lobby that launched a logic
	 */
	public GameLogic(Lobby lobby){
		preparePlayers(lobby);
		prepareDeck();
		prepareBoard();
		nbDice = lobby.getParam().getNbrDice();
	}
	
	/**
	 * Generate the logic board
	 */
	private void prepareBoard() {
		// TODO SPRINT 4 récupérer la liste des case et les placer dans l'ordre
		
		/*for(Square s : dbSqaures){
		
		}*/
	}
	
	/**
	 * Generate a random deck
	 */
	private void prepareDeck() {
		Deck = new ArrayDeque<>(NB_DECKCARD);
		//TODO SPRINT 4 récupérer la liste de carte de la DB, idéalement une correspondance entre la carte et la quantité accepté
		
		for(int i = 0; i < NB_DECKCARD; ++i){
			//Deck.addFirst(new Card());
		}
	}
	
	/**
	 * create and store the players with their own possessions.
	 * @param lobby
	 */
	private void preparePlayers(Lobby lobby) {
		joueurs = new ArrayDeque<>(lobby.getPlayers().size());
		ArrayList<ClientHandler> tab = new ArrayList<>(lobby.getPlayers());
		Random rdm = new Random();
		
		while(!tab.isEmpty()){
			int pos = rdm.nextInt(tab.size());
			joueurs.addFirst(new Player(tab.remove(pos), lobby.getParam().getMoneyAtTheStart()));
		}
	}
	
	@Override public void run() {
		
		// TODO déroulement du jeu (sprint 4/5)
	}
}
