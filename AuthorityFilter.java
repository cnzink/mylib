

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import java.io.*;

@WebFilter(filterName="authority"
	, urlPatterns={"/*"}
	, initParams={
		//@WebInitParam(name="encoding", value="GBK"),
		@WebInitParam(name="encoding", value="UTF-8"),
		@WebInitParam(name="loginPage", value="/index.jsp"),
		@WebInitParam(name="proLogin", value="/proLogin.jsp")})
public class AuthorityFilter implements Filter 
{
	//FilterConfig�����ڷ���Filter��������Ϣ
	private FilterConfig config;
	//ʵ�ֳ�ʼ������
	public void init(FilterConfig config)
	{
		this.config = config; 
	}
	//ʵ�����ٷ���
	public void destroy()
	{
		this.config = null; 
	}
	//ִ�й��˵ĺ��ķ���
	public void doFilter(ServletRequest request,
		ServletResponse response, FilterChain chain)
		throws IOException,ServletException
	{
		String encoding = config.getInitParameter("encoding");
		//String loginPage = config.getInitParameter("loginPage");
		request.setCharacterEncoding(encoding);
		HttpServletRequest requ = (HttpServletRequest)request;
		HttpSession session = requ.getSession(true);
		//if(session.getAttribute("user") != null )
	//	{
			//	System.out.println("filter pass");
		//		chain.doFilter(request, response);
		//}
		//��ȡ��Filter�����ò���
				
		//String proLogin = config.getInitParameter("proLogin");
		//����request�����õ��ַ���
					//��
		
		HttpServletResponse respo = (HttpServletResponse)response;
		
		//��ȡ�ͻ������ҳ��
		String requestPath = requ.getServletPath();
		//System.out.println("this is filter"+requestPath);
		//���session��Χ��userΪnull��������û�е�¼
		//���û�����ļȲ��ǵ�¼ҳ�棬Ҳ���Ǵ����¼��ҳ��
				
		if(session.getAttribute("user") != null||requestPath.endsWith("index.jsp")||requestPath.endsWith(".png")||requestPath.endsWith(".gif")||requestPath.endsWith("LoginVerify"))
		//if( session.getAttribute("username") == null)
		{
		//	System.out.println("filter pass");
			chain.doFilter(request, response);
		}
		else
		{
			//forward����¼ҳ��
			//System.out.println("filter no pass");
			//request.setAttribute("tip" , "You have not logged in the system. Please input you username and password");
			//request.getRequestDispatcher(loginPage)
			//	.forward(request, response);
			respo.sendRedirect("index.jsp");			
			//chain.doFilter(request, response);
		}
		
		//chain.doFilter(request, response);
	} 
}
