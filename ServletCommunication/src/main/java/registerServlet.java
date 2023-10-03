import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/registerServlet")
public class registerServlet extends HttpServlet {
	private Connection conn;
	private PreparedStatement ps;
	private int resultSet;
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext context = config.getServletContext();
	    String dbUser = context.getInitParameter("db_user");
	    String dbPassword = context.getInitParameter("db_password");
	try {
			Class.forName("org.mariadb.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mariadb://mariadb.vamk.fi/e2101089_java_demo","e2101089","h8uV4GzDUmb");
			ps = conn.prepareStatement("INSERT INTO user (email, password) VALUES(?, ?)");
		} catch(SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String email = req.getParameter("email");
		System.out.println(email);
		String password = req.getParameter("password");
		RequestDispatcher reqDis;
		if (!isValidEmail(email)) {
	        reqDis = req.getRequestDispatcher("homeServlet");
	        req.setAttribute("message", "Invalid email format");
	        reqDis.include(req, res);
	        return; // Exit the method, no need to proceed further
	    }
	    try {
	        // Check if the email already exists in the database
	        PreparedStatement checkEmailPS = conn.prepareStatement("SELECT * FROM user WHERE email = ?");
	        checkEmailPS.setString(1, email);
	        ResultSet emailResult = checkEmailPS.executeQuery();
	        if (emailResult.next()) {
	            // Email already exists, treat it as unsuccessful
	            reqDis = req.getRequestDispatcher("homeServlet");
	            req.setAttribute("message", "Email already exists");
	            reqDis.include(req, res);
	        } else {
	            // Email doesn't exist, proceed with registration
	            ps.setString(1, email);
	            ps.setString(2, password);
	            resultSet = ps.executeUpdate();
	if (resultSet > 0) {
		//login success
		System.out.println("Demo");
		reqDis = req.getRequestDispatcher("homeServlet");
		req.setAttribute("message", "registration successfully");
		reqDis.forward(req, res);
	} else {
		//login unsuccessful
		reqDis = req.getRequestDispatcher("homeServlet");
		req.setAttribute("message", "registration unsuccessfully");
		reqDis.include(req, res);
		}
		}
	} catch(SQLException e) {
			e.printStackTrace();
		}
}
	    		private boolean isValidEmail(String email) {
		// TODO Auto-generated method stub
		return false;
	}
				public class MyRegexUtils{
	    			public static boolean isValidEmail(String email) {
	    			String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";       		
	    			Pattern pattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
	    			System.out.println(email);
	    			Matcher matcher = pattern.matcher(email);
	    	
	    			return matcher.matches();
	        }
	    		}
	@Override
	public void destroy() {
		try {
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

