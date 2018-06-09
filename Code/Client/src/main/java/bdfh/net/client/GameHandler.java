package bdfh.net.client;

import bdfh.gui.controller.Controller_board;
import bdfh.gui.controller.Controller_lobbyList;
import bdfh.logic.usr.Player;
import bdfh.net.protocol.GameProtocol;
import bdfh.net.protocol.Protocoly;
import bdfh.serializable.GsonSerializer;
import bdfh.serializable.LightBoard;
import bdfh.serializable.LightPlayer;
import bdfh.serializable.LightSquare;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static bdfh.net.protocol.GameProtocol.*;

/**
 * @author Héléna Line Reymond
 * @author Daniel Gonzalez Lopez
 * @version 1.0
 */
public class GameHandler extends Thread {
	
	private static Logger LOG = Logger.getLogger("GameHandler");
	
	private Map<Integer, LightPlayer> players = new HashMap<>();
	
	private LightBoard board = null;
	
	private BufferedReader in = null;
	private PrintWriter out = null;
	private String response;
	
	private Controller_board sub;
	
	private GameHandler() {}
	
	private static class Instance {
		
		static final GameHandler instance = new GameHandler();
	}
	
	public static GameHandler getInstance() {
		
		return Instance.instance;
	}
	
	/**
	 * Initialise the game handler with the streams of the client.
	 *
	 * @param in Reader stream.
	 * @param out Writer stream.
	 */
	public void initialise(BufferedReader in, PrintWriter out) {
		
		this.in = in;
		this.out = out;
	}
	
	public void setSub(Controller_board board) {
		
		sub = board;
	}
	
	/**
	 * Handle the command received in the game.
	 *
	 * @param line Command received.
	 */
	private void handleGame(String line) {
		
		if (sub != null) {
			sub.errorMessage("");
		}
		
		String[] split = line.split(" ", 2);
		
		String[] param;
		
		int pos;
		int id;
		LightSquare square;
		
		switch (split[0]) {
			case GAM_BOARD:
				manageBoard(split[1]);
				break;
			
			case GAM_PLYR:
				managePlayers(split[1]);
				break;
				
			case GAM_WIN:
				id = Integer.parseInt(split[1]);
				
				sub.logMessage(id, players.get(id).getUsername() + " a gagné la partie ! BRAVO");
				
				if (Player.getInstance().getID() == id) {
					// TODO - START SOUND
				}
				
				break;
			
			
			case GAM_PLAY:
				manageCurrentPlayer(split[1]);
				break;
			
			case GAM_ROLL:
				param = split[1].split(" ");
				manageRoll(param);
				break;
				
			case GAM_DRAW:
				param = split[1].split(" ", 2);
				manageDraw(Integer.parseInt(param[0]), param[1]);
				break;
			
			case GAM_GAIN:
				param = split[1].split(" ");
				manageGain(param);
				break;
				
			case GAM_PAY:
				param = split[1].split(" ");
				managePay(param);
				break;
				
			case GAM_MOV:
				param = split[1].split(" ");
				manageMove(param);
				break;
				
			case GAM_EXAM:
				param = split[1].split(" ");
				id = Integer.parseInt(param[0]);
				
				players.get(id).setInExam(true);
				manageMove(param);
				
				// Refresh
				sub.updateBoard();
				
				break;
			
			case GAM_FRDM:
				id = Integer.parseInt(split[1]);
				
				players.get(id).setInExam(false);
				
				// Refresh
				sub.updateBoard();
				sub.logMessage(id, players.get(id).getUsername() + " sort de la salle d'examen.");
				
				break;

			case GAM_FRDM_C:

				// The player received a freedom card
				
				id = Integer.parseInt(split[1]);
				
				players.get(id).setFreeCards(1);
				
				// Update the player
				if(id == Player.getInstance().getID()) {
					Player.getInstance().setHasFreedomCard(true);
				}
				
				// Refresh
				sub.updateBoard();
				sub.logMessage(id, players.get(id).getUsername() + " a reçu une carte pour sortir d'examen.");
				
				break;

			case GAM_FRDM_U:

				// The player used a freedom card
				id = Integer.parseInt(split[1]);
				
				players.get(id).setFreeCards(-1);
				players.get(id).setInExam(false);
				
				// Update the player
				if(players.get(id).getFreeCards() == 0 && id == Player.getInstance().getID()) {
					
					Player.getInstance().setHasFreedomCard(false);
				}
				
				// Refresh
				sub.updateBoard();
				sub.logMessage(id, players.get(id).getUsername() + " utilise une carte pour sortir d'examen.");
				
				break;
				
			case GAM_BUYS:
				param = split[1].split(" ");
				
				pos = Integer.parseInt(param[1]);
				id = Integer.parseInt(param[0]);
				square = board.getSquares().get(pos);
				
				square.setOwner(players.get(Integer.parseInt(param[0])));
				
				sub.setOwner(pos, id);
				sub.logMessage(id, players.get(id).getUsername() + " a acheté la salle " + square.getName());
				
				break;
			
			case GAM_SELL:
				param = split[1].split(" ");
				
				pos = Integer.parseInt(param[1]);
				id = Integer.parseInt(param[0]);
				square = board.getSquares().get(pos);
				
				square.setOwner(null);
				
				sub.setOwner(pos, -1);
				sub.logMessage(id, players.get(id).getUsername() + " a vendu " + square.getName());
				
				break;
			
			case GAM_BCOUCH:
				param = split[1].split(" ");
				
				id = Integer.parseInt(param[0]);
				pos = Integer.parseInt(param[1]);
				square = board.getSquares().get(pos);
				
				square.toggleCouch(1);
				
				sub.redrawSquare(pos);
				sub.logMessage(id, players.get(id).getUsername() + " a acheté un canapé pour la salle " + square.getName());
				
				break;
			
			case GAM_SCOUCH:
				param = split[1].split(" ");
				
				id = Integer.parseInt(param[0]);
				pos = Integer.parseInt(param[1]);
				square = board.getSquares().get(pos);
				
				square.toggleCouch(-1);
				
				sub.redrawSquare(pos);
				sub.logMessage(id, players.get(id).getUsername() + " a vendu un canapé de la salle " + square.getName());
				
				break;
			
			case GAM_BHCINE:
				param = split[1].split(" ");
				
				id = Integer.parseInt(param[0]);
				pos = Integer.parseInt(param[1]);
				square = board.getSquares().get(pos);
				
				square.toggleHCine(true);
				
				sub.redrawSquare(Integer.parseInt(param[1]));
				sub.logMessage(id, players.get(id).getUsername() + " a acheté un home cinéma pour la salle " + square.getName());
				
				break;
			
			case GAM_SHCINE:
				param = split[1].split(" ");
				
				id = Integer.parseInt(param[0]);
				pos = Integer.parseInt(param[1]);
				square = board.getSquares().get(pos);
				
				square.toggleHCine(false);
				
				sub.redrawSquare(pos);
				sub.logMessage(id, players.get(id).getUsername() + " a vendu le home cinéma de la salle " + square.getName());
				
				break;
				
			case GAM_HYPOT:
				param = split[1].split(" ");
				
				id = Integer.parseInt(param[0]);
				pos = Integer.parseInt(param[1]);
				square = board.getSquares().get(pos);
				
				square.setMortgage();
				
				sub.logMessage(id, players.get(id).getUsername() + " a mis en hypothèque la salle " + square.getName());
				
			case Protocoly.ANS_ERR:
				sub.errorMessage(split[1]);
				
				LOG.log(Level.INFO, split[1]);
				
				break;
			
		}
	}
	
	@Override
	public void run() {
		
		try {
			
			// Start the game (interface)
			Controller_lobbyList.startGame();
			
			while (true) {
				response = in.readLine();
				LOG.log(Level.INFO, "RECEIVED: " + response);
				
				handleGame(response);
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "GameHandler::run: " + e);
		}
	}
	
	public void rollDice() {
		
		sendData(GAM_ROLL);
	}
	
	public void endTurn() {
		
		Player.getInstance().setMyTurn(false);
		sub.notifyTurn();
		sendData(GAM_ENDT);
	}
	
	public Map<Integer, LightPlayer> getPlayers() {
		
		return players;
	}
	
	public LightBoard getBoard() {
		
		return board;
	}
	
	/**
	 * Send data (commands) to the server.
	 *
	 * @param data Data to send.
	 */
	private void sendData(String data) {
		
		LOG.log(Level.INFO, "SENT: " + data);
		
		// Print the data and flush the stream.
		out.println(data);
		out.flush();
	}
	
	private void manageRoll(String[] str) {
		
		ArrayList tmp = new ArrayList();
		int id = Integer.parseInt(str[0]);
		
		for (int i = 2; i < str.length; ++i) {
			tmp.add(Integer.parseInt(str[i]));
		}
		
		sub.logMessage(id, players.get(id).getUsername() + " tire les dés : " + tmp);
		
		// Don't move the player if he's in exam
		if(!players.get(id).isInExam()) {
			
			sub.movePawn(Integer.parseInt(str[0]), tmp);
		}
	}
	
	private void managePlayers(String json) {
		
		JsonArray jsonPlayers = GsonSerializer.getInstance()
				.fromJson(json, JsonArray.class);
		
		for (JsonElement je : jsonPlayers) {
			JsonObject jo = je.getAsJsonObject();
			
			LightPlayer tmp = LightPlayer.instancify(jo);
			
			players.put(tmp.getId(), tmp);
		}
	}
	
	private void manageBoard(String json) {
		
		board = LightBoard.instancify(json);
		
		synchronized (this) {
			this.notify();
		}
	}
	
	private void manageCurrentPlayer(String playerID) {
		
		int id = Integer.parseInt(playerID);
		String username = Player.getInstance().getUsername();
		String usernameTurn = players.get(id).getUsername();
		
		if (sub == null) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (username.equals(usernameTurn)) {
			Player.getInstance().setMyTurn(true);
			
			sub.notifyTurn();
			
			if (players.get(id).isInExam()) {
				
				sub.loadPopup();
			}
		}
		
		sub.logMessage(id, "C'est au tour du joueur " + usernameTurn);
		
		synchronized (this) {
			this.notify();
		}
	}

	private void manageGain(String[] split) {
		
		int id = Integer.parseInt(split[0]);
		int gain = Integer.parseInt(split[1]);
		
		updateCapital(id, gain);
		
		sub.logMessage(id, players.get(id).getUsername() + " gagne " + gain + ".-");
	}
	
	private void managePay(String[] split) {
		
		int id = Integer.parseInt(split[0]);
		int pay = Integer.parseInt(split[1]);
		
		updateCapital(id, -1 * pay);
		
		sub.logMessage(id, players.get(id).getUsername() + " paie " + pay + ".-");
	}
	
	private void updateCapital(int id, int value) {
		
		players.get(id).addCapital(value);
		
		sub.updateBoard();
	}
	
	private void manageMove(String[] split) {
		
		int id = Integer.parseInt(split[0]);
		int pos = Integer.parseInt(split[1]);
	
		sub.move(id, pos);
		
		sub.logMessage(id, players.get(id).getUsername() + " bouge à la case " + board.getSquares().get(pos).getName());
	}
	
	/**
	 * Use a card the leave the exam.
	 */
	public void useFreedomCard() {
		sendData(GAM_FRDM_U);
	}
	
	/**
	 * Pay the tax to leave the exam.
	 */
	public void payExamTax() {
		
		sendData(GAM_FRDM_T);
	}
	
	public void manageDraw(int id, String card) {
		
		sub.logMessage(id, players.get(id).getUsername() + " a tiré la carte : " + card);
	}
}