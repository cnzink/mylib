

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class LoginVerify
 */
@WebServlet("/LoginVerify")
public class LoginVerify extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginVerify() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String strUsr= request.getParameter("username");
		String strPass=request.getParameter("userpasswd");
		String strUsrServer,strPassServer;
		HttpSession session = request.getSession(true);
		boolean loginSuccess=false;
		
		//String JsonContext = new Util().ReadFile("I:\\usrinfo.json");
		String JsonContext = new Util().ReadFile("/tomcatwww/usrinfo.json");
		JSONArray jsonArray = JSONArray.fromObject(JsonContext); 
		int size = jsonArray.size();
		//System.out.println("Size: " + size);
		for(int  i = 0; i < size; i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			//System.out.println("[" + i + "]username=" + jsonObject.get("username"));
			//System.out.println("[" + i + "]userpasswd=" + jsonObject.get("userpasswd"));
			strUsrServer=jsonObject.get("username").toString();
			strPassServer=jsonObject.get("userpasswd").toString();
			if(strUsr.equals(strUsrServer)&&strPass.equals(strPassServer))
			{
				 
				loginSuccess=true;
				//RequestDispatcher disp=request.getRequestDispatcher("filter.jsp");
				//disp.forward(request, response);
				session.setAttribute("user", strUsr);
				
				response.sendRedirect("test16.jsp");
			}
					
		}
		if(loginSuccess==false)
		{
			response.sendRedirect("2index.jsp");
			
		}
		
		
		
		//JSONObject jsonObject=new JSONObject();
	  //	jsonObject.put("name","jake");
		//jsonObject.put("package_name","hahah");
		//jsonObject.put("check_version","good");
		
		
	}

}
