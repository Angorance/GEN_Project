package bdfh.serializable;

import bdfh.game.Lobby;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;

public class LightLobby {
	
	private int ID;
	private ArrayList<String> usernames = new ArrayList<>(Lobby.MAX_PLAYER);
	private ArrayList<Boolean> areReady = new ArrayList<>(Lobby.MAX_PLAYER);
	
	
	public LightLobby() {}
	
	public int getID() {
		
		return ID;
	}
	
	public ArrayList<String> getUsernames() {
		
		return usernames;
	}
	
	public ArrayList<Boolean> getAreReady() {
		
		return areReady;
	}
	
	public void setID(int ID) {
		
		this.ID = ID;
	}
	
	public void addPlayer(String username/*, int index*/) {
	
		usernames.add(/*index, */username);
		areReady.add(false);
	}
	
	public void addReady(boolean ready, int index) {
		
		areReady.set(index, ready);
	}
	
	public void removePlayer(int index) {
		
		usernames.remove(index);
		areReady.remove(index);
	}
	
	public String jsonify() {
		
		JsonObject jsonLobby = new JsonObject();
		
		JsonArray jsonUsers = new JsonArray();
		
		for (String s : usernames) {
			
			jsonUsers.add(s);
		}
		
		JsonArray jsonReady = new JsonArray();
		
		for (boolean b : areReady) {
			
			jsonReady.add(b);
		}
		
		jsonLobby.add("ID", new JsonPrimitive(ID));
		jsonLobby.add("Users", jsonUsers);
		jsonLobby.add("Ready", jsonReady);
		
		return GsonSerializer.getInstance().toJson(jsonLobby);
	}
}
