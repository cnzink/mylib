

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
	//FilterConfig可用于访问Filter的配置信息
	private FilterConfig config;
	//实现初始化方法
	public void init(FilterConfig config)
	{
		this.config = config; 
	}
	//实现销毁方法
	public void destroy()
	{
		this.config = null; 
	}
	//执行过滤的核心方法
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
		//获取该Filter的配置参数
				
		//String proLogin = config.getInitParameter("proLogin");
		//设置request编码用的字符集
					//①
		
		HttpServletResponse respo = (HttpServletResponse)response;
		
		//获取客户请求的页面
		String requestPath = requ.getServletPath();
		//System.out.println("this is filter"+requestPath);
		//如果session范围的user为null，即表明没有登录
		//且用户请求的既不是登录页面，也不是处理登录的页面
				
		if(session.getAttribute("user") != null||requestPath.endsWith("index.jsp")||requestPath.endsWith(".png")||requestPath.endsWith(".gif")||requestPath.endsWith("LoginVerify"))
		//if( session.getAttribute("username") == null)
		{
		//	System.out.println("filter pass");
			chain.doFilter(request, response);
		}
		else
		{
			//forward到登录页面
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
