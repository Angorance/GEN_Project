package bdfh.gui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller_formLobby implements Initializable {
	
	@FXML private AnchorPane paneFond;
	@FXML private Label random_label;
	@FXML private JFXTextField nameLobby;
	@FXML private JFXTextField moneyStart;
	@FXML private JFXComboBox<Integer> numberDice;
	@FXML private JFXCheckBox randomCheck;
	@FXML private JFXButton returnButton;
	@FXML private JFXButton accepteButton;
	
	private Controller_lobbyList cl;
	
	public Controller_formLobby(Controller_lobbyList cl) {
		this.cl = cl;
	}
	
	/**
	 * Create the lobby
	 */
	private void formValidation(){
		String name = nameLobby.getText();
		int nbrDice = numberDice.getValue();
		int money = Integer.parseInt(moneyStart.getText());
		boolean random = randomCheck.isSelected();
		
		//TODO création du lobby
		cl.createItem(null/*TODO mettre l'objet lobby ici*/);
		
	}
	
	/**
	 * Return to the list of lobby page
	 */
	private void formReturn() {
		cl.createItem(null);
	}
	
	/**
	 * Generate de item in comboBox
	 */
	private void generateItemComboBox(){
		ObservableList<Integer> items = FXCollections.observableArrayList();
		
		/*TODO retrieve of the limit of the number of dice */
		for(int i = 2; i <= 4; ++i){
			items.add(i);
		}
		numberDice.setItems(items);
	}
	
	
	@Override public void initialize(URL location, ResourceBundle resources) {
		generateItemComboBox();
		
		accepteButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override public void handle(ActionEvent event) {
				formValidation();
			}
		});
		
		returnButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override public void handle(ActionEvent event) {
				formReturn();
			}
		});
		
	}
	

}
