package bdfh.logic.saloon;

import bdfh.net.server.ClientHandler;
import bdfh.serializable.GsonSerializer;
import bdfh.serializable.Parameter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used to simulate a logic with players.
 *
 * @author Héléna Line Reymond
 * @author Daniel Gonzalez Lopez
 * @version 1.0
 */
public class Lobby {
	
	public static int nbLobbies = 0;
	public static final int MAX_PLAYER = 4;
	
	private int ID;
	private int numOfReady = 0;
	
	private Parameter param;
	private ArrayList<ClientHandler> players = new ArrayList<>(MAX_PLAYER);
	private ArrayList<Boolean> areReady = new ArrayList<>(MAX_PLAYER);
	
	boolean isRunning = false;
	
	private final static Logger LOG = Logger.getLogger("NotificationHandler");
	
	
	public Lobby(Parameter param) {
		
		this.param = param;
		setID();
	}
	
	// -------------------------------------------------------------------------
	// LOBBY METHODS -----------------------------------------------------------
	
	/**
	 * Add a player to the logic.
	 *
	 * @param player Player who wants to join the logic.
	 */
	public synchronized void joinLobby(ClientHandler player) {
		
		players.add(player);
		areReady.add(false);
	}
	
	public synchronized void setReady(ClientHandler player) {
		
		areReady.set(players.indexOf(player), true);
		++numOfReady;
		
		// TODO - Start logic (GameLogic)
		if (players.size() > 1 && numOfReady == players.size()) {
			LOG.log(Level.INFO, "Lobby" + ID + ": ALL PLAYERS READY");
		} else if (players.size() > 1 && numOfReady > 1) {
			LOG.log(Level.INFO, "Lobby" + ID + ": AT LEAST 2 PLAYERS READY");
		}
	}
	
	/**
	 * Remove a player from the logic.
	 *
	 * @param player Player who wants to quit the logic.
	 */
	public synchronized void quitLobby(ClientHandler player) {
		
		// TODO - Do it from the Lobbies to be able to notify the Notification observer !!!
		
		if (!areReady.isEmpty()
				&& areReady.get(players.indexOf(player)) != null) {
			areReady.remove(players.indexOf(player));
			--numOfReady;
		}
		
		players.remove(player);
		
		if (players.isEmpty()) {
			Lobbies.getInstance().removeLobby(this);
		}
	}
	
	/**
	 * TODO
	 *
	 * @return
	 */
	public boolean isRunning() {
		
		return isRunning;
	}
	
	public synchronized void setID() {
		
		ID = nbLobbies++;
	}
	
	public synchronized ArrayList<ClientHandler> getPlayers() {
		
		return players;
	}
	
	public boolean isFull() {
		
		return (getPlayers().size() == 4);
	}
	
	public Parameter getParam() {
		
		return param;
	}
	
	public int getID() {
		
		return ID;
	}
	
	// SERIALISATION -----------------------------------------------------------
	
	public String jsonify() {
		
		JsonObject jsonLobby = new JsonObject();
		
		JsonArray jsonUsers = new JsonArray();
		
		for (ClientHandler ch : players) {
			
			jsonUsers.add(ch.getClientUsername());
		}
		
		JsonArray jsonReady = new JsonArray();
		
		for (boolean b : areReady) {
			
			jsonReady.add(b);
		}
		
		jsonLobby.add("ID", new JsonPrimitive(ID));
		jsonLobby.add("Mode", new JsonPrimitive(param.getMode()));
		jsonLobby.add("Time", new JsonPrimitive(param.getTime()));
		jsonLobby.add("Users", jsonUsers);
		jsonLobby.add("Ready", jsonReady);
		
		return GsonSerializer.getInstance().toJson(jsonLobby);
	}
}