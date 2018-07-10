package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import db.mysql.MySQLTableCreation;
import entity.Item;
import external.TicketMasterAPI;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));

		// term can be empty
		String term = request.getParameter("term");
		String userId = request.getParameter("user_id");
		DBConnection connection = DBConnectionFactory.getConnection();
		
		System.out.println("the raw userId is: " + userId);
		StringBuilder sb = new StringBuilder();
		int startIndex = 0;
		for (int i = 0; i < userId.length() - 1; i++) {
			if (userId.charAt(i) == '.') {
				System.out.println("I found a .!");
				sb.append(userId, startIndex, i);
				startIndex = i + 1;
			}
		}
		userId = (startIndex == 0) ? userId : sb.toString();
		System.out.println("modified userId: " + userId);

		//MySQLTableCreation.resetTable();

		if (connection.getFullname(userId) == null) {
			System.out.println("Cannot find this userId!");
			MySQLTableCreation.createUser(userId);
		}
		
		try {
			List<Item> items = connection.searchItems(lat, lon, term);
			Set<String> favoriteItems = connection.getFavoriteItemIds(userId);

			JSONArray array = new JSONArray();
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.put("favorite", favoriteItems.contains(item.getItemId()));
				array.put(obj);
			}
			RpcHelper.writeJsonArray(response, array);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}

	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
