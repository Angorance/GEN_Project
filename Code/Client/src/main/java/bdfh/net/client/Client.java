package bdfh.net.client;

import bdfh.exceptions.*;
import bdfh.logic.usr.*;
import bdfh.net.protocol.Protocoly;
import bdfh.serializable.*;

import java.io.*;
import java.net.Socket;

import static bdfh.net.protocol.Protocoly.*;

// TODO - Maybe handle better the exceptions...

/**
 * Client class.
 * Implements the methods needed to communicate with the server.
 *
 * @author Daniel Gonzalez Lopez
 * @version 1.0
 */
public class Client {
	
	private Socket clientSocket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	
	private String response;
	
	
	private Client() {}
	
	private static class Instance {
		
		static final Client instance = new Client();
	}
	
	public static Client getInstance() {
		
		return Instance.instance;
	}
	
	/**
	 * Connect to the server and check if it is successful.
	 *
	 * @throws ConnectionException if the server sends a wrong answer.
	 * @throws IOException
	 */
	public void connect() throws ConnectionException, IOException {
		
		try {
			clientSocket = new Socket(Protocoly.SERVER, Protocoly.CPORT);
			in = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream());
			
			response = in.readLine();
			
			if (!handleConnectionAnswer(response)){
				throw new ConnectionException(
						"A problem happened during Connection");
			}
			
			// Connect to the notification channel
			Notification.getInstance().connect();
			
		} catch (IOException e) {
			System.out.println("Client::connect: " + e);
			throw e;
		}
		
		
	}
	
	/**
	 * Disconnect from the server and check if it is successful.
	 *
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		
		sendData(Protocoly.CMD_BYE);
		
		try {
			response = in.readLine();
			
			if (!response.equals(Protocoly.ANS_BYE)) {
				// TODO - Disconnection failed - What to do?
			}
		} catch (IOException e) {
			System.out.println("Client::disconnect: " + e);
			throw e;
		} finally {
			
			if (in != null) {
				in.close();
			}
			
			if (out != null) {
				out.close();
			}
			
			if (clientSocket != null) {
				clientSocket.close();
			}
		}
	}
	
	/**
	 * Register a user given his credentials.
	 * Fails if the username is not available.
	 *
	 * @param usr Username of the user.
	 * @param password Password of the user.
	 *
	 * @return True if registration successful, false otherwise.
	 */
	public boolean register(String usr, String password)
			throws CredentialsException, IOException {
		
		boolean success;
		
		sendData(CMD_RGSTR + " " + usr + " " + password);
		
		try {
			response = in.readLine();
			
			if (response.equals(ANS_SUCCESS)) {
				success = true;
			} else if (response.equals(ANS_DENIED)) {
				success = false;
			} else {
				throw new CredentialsException("Problem with Registration");
			}
		} catch (IOException e) {
			System.out.println("Client::register: " + e);
			throw e;
		}
		
		return success;
	}
	
	/**
	 * Log the user in given his credentials.
	 * Fails if the username does not exist or if the credentials does not
	 * match.
	 *
	 * @param usr Username of the user.
	 * @param password Password of the user.
	 *
	 * @return 1 if login successful, 0 if username unknown, -1 if password does
	 * 		not match with username.
	 *
	 * @throws CredentialsException if the server sends wrong answer.
	 * @throws IOException
	 */
	public int login(String usr, String password)
			throws CredentialsException, IOException {
		
		int result;
		
		sendData(CMD_LOGIN + " " + usr + " " + password);
		
		try {
			response = in.readLine();
			
			if (response.equals(ANS_SUCCESS)) {
				result = 1;
			} else if (response.equals(ANS_UKNW)) {
				result = 0;
			} else if (response.equals(ANS_DENIED)) {
				result = -1;
			} else {
				throw new CredentialsException("Problem with Login");
			}
		} catch (IOException e) {
			System.out.println("Client::login: " + e);
			throw e;
		}
		
		return result;
	}
	
	/**
	 * Send data (commands) to the server.
	 *
	 * @param data Data to send.
	 */
	private void sendData(String data) {
		
		// Print the data and flush the stream.
		out.println(data);
		out.flush();
	}
	
	private boolean handleConnectionAnswer(String response) {
		
		String[] splitted = response.split(" ", 2);
		
		if (!splitted[0].equals(ANS_CONN)) {
			return false;
		}
		
		Player.setBounds(GsonSerializer.getInstance()
				.fromJson(splitted[1], BoundParameters.class));
		
		return true;
	}
	
	/**
	 * Create a new lobby on the game server.
	 *
	 * @param parameters    Parameters used in the new lobby.
	 *
	 * @return  true if the lobby is created, false otherwise.
	 */
	public boolean createLobby(Parameter parameters) {
		
		boolean result = false;
		
		// Send the new lobby to the server with its parameters
		String jsonParam = GsonSerializer.getInstance().toJson(parameters);
		sendData(CMD_NEWLOBBY + " " + jsonParam);
		
		try {
			response = in.readLine();
			
			if (response.equals(ANS_SUCCESS)) {
				result = true;
			} else if (response.equals(ANS_DENIED)) {
				result = false;
			}
			
		} catch (IOException e) {
			System.out.println("Client::createLobby: " + e);
			result = false;
		}
		
		return result;
	}
	
	/**
	 * Join a lobby on the game server.
	 *
	 * @param lobbyID   ID of the lobby to join.
	 *
	 * @return true if the lobby is joined, false otherwise.
	 */
	public boolean joinLobby(int lobbyID) {
		
		boolean result = false;
		
		sendData(CMD_JOIN + " " + lobbyID);
		
		try {
			response = in.readLine();
			
			if (response.equals(ANS_SUCCESS)) {
				result = true;
			} else if (response.equals(ANS_DENIED)) {
				result = false;
			}
			
		} catch (IOException e) {
			System.out.println("Client::joinLobby: " + e);
			result = false;
		}
		
		return result;
	}
	
	/**
	 * The player notifies the game server that he's ready.
	 *
	 * @return  true if the lobby is left, false otherwise.
	 */
	public boolean setReady() {
		
		boolean result = false;
		
		// Send the command to the server
		sendData(CMD_RDY);
		
		try {
			response = in.readLine();
			
			if (response.equals(ANS_SUCCESS)) {
				result = true;
			} else if (response.equals(ANS_DENIED)) {
				result = false;
			}
			
		} catch (IOException e) {
			System.out.println("Client::setReady: " + e);
			result = false;
		}
		
		return result;
	}
	
	/**
	 * Quit the current lobby on the game server.
	 *
	 * @return  true if the lobby is left, false otherwise.
	 */
	public boolean quitLobby() {
		
		boolean result = false;
		
		// Send the command to the server
		sendData(CMD_QUITLOBBY);
		
		try {
			response = in.readLine();
			
			if (response.equals(ANS_SUCCESS)) {
				result = true;
			} else if (response.equals(ANS_DENIED)) {
				result = false;
			}
			
		} catch (IOException e) {
			System.out.println("Client::quitLobby: " + e);
			result = false;
		}
		
		return result;
	}
}
