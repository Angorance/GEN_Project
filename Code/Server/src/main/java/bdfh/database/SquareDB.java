package bdfh.database;

import bdfh.logic.game.*;
import java.sql.*;
import java.util.*;

/**
 * Class used to execute queries on the square table.
 *
 * @author Héléna Line Reymond
 * @version 1.0
 */
public class SquareDB {
	
	private static final DatabaseConnect db = DatabaseConnect.getInstance();
	
	private SquareDB() {}
	
	/**
	 * Internal static class used to create one and only one instance of
	 * SquareDB to guarantee it follows the singleton model.
	 */
	private static class Instance {
		
		static final SquareDB instance = new SquareDB();
	}
	
	/**
	 * Get the only instance of SquareDB.
	 *
	 * @return the instance of SquareDB.
	 */
	protected static SquareDB getInstance() {
		
		return Instance.instance;
	}
	
	/**
	 * Get all squares stored in database.
	 *
	 * @return array of all squares.
	 */
	public ArrayList<Square> getSquares() {
		
		ArrayList<Square> squares = new ArrayList<>();
		
		String sql = "SELECT S.position, S.family, S.name, P.rent, P.price, P.priceCouch, P.priceHomeCinema, P.hypothec "
				+ "FROM square AS S "
				+ "LEFT JOIN price AS P ON S.price_id = P.id "
				+ "ORDER BY S.position;";
		
		try {
			
			// Execute and get the result of the query
			Statement statement = db.connect().createStatement();
			ResultSet result = statement.executeQuery(sql);
			
			// Get the squares
			while(result.next()) {
				
				int position = result.getInt("position");
				String family = result.getString("family");
				String name = result.getString("name");
				int rent = result.getInt("rent");
				int price = result.getInt("price");
				int priceCouch = result.getInt("priceCouch");
				int priceHomeCinema = result.getInt("priceHomeCinema");
				int hypothec = result.getInt("hypothec");

				// Create the price if needed
				Price prices = null;
				
				if(rent != 0) {
					prices = new Price(rent, price, priceCouch, priceHomeCinema, hypothec);
				}
				
				// Create one card
				Square square = new Square(position, family, name, prices);
				squares.add(square);
			}
			
			// Close the db
			statement.close();
			
		} catch (SQLException e) {
			System.out.print("The database can't get the squares : ");
			e.printStackTrace();
			
		} finally {
			db.disconnect();
		}
		
		return squares;
	}
}
