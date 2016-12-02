

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class ModifyPasswd
 */
@WebServlet("/ModifyPasswd")
public class ModifyPasswd extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModifyPasswd() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String strUsrServer,strPassServer;
		String oldPass=request.getParameter("oldpasswd");
		String newPass=request.getParameter("newpasswd");
		boolean modiSuccess=false;
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
			if(oldPass.equals(strPassServer))
			{
					
				
				modiSuccess=true;
				//RequestDispatcher disp=request.getRequestDispatcher("filter.jsp");
				//disp.forward(request, response);
				 JSONObject newJsonObject=new JSONObject();
				 newJsonObject.put("username",strUsrServer);  
				 newJsonObject.put("userpasswd",newPass);
				 String newstr="["+newJsonObject.toString()+",]";
				 
				 System.out.println(newstr);
				
				 try
					{BufferedWriter bw = new BufferedWriter(new FileWriter("/tomcatwww/usrinfo.json"));
					   
					   bw.write(newstr);
					   bw.flush();
					   bw.close();
					   response.sendRedirect("passwdok.html");
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				
			}
					
		}
		if(modiSuccess==false)
		{
			response.sendRedirect("passwderror.html");
			
		}
		
		
	}

}
