import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

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
		String password = req.getParameter("password");
		try {
			ps.setString(1, email);
			ps.setString(2, password);
			int result = ps.executeUpdate();
			RequestDispatcher reqDis;
	if (result > 0) {
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
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void destroy( ) {
		try {
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}

