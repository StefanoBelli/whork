package logic.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import logic.util.Util;

public class MustBeLoggedInFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		
		if(req.getSession().getAttribute("user") == null) {
			if(!Util.cookieLogin(req, (HttpServletResponse)response, req.getRequestURI())) {
				req.setAttribute("showMustLoginInfo", true);
				req.getRequestDispatcher("login.jsp").forward(request, response);
			}
		} else {
			req.getRequestDispatcher(req.getRequestURI()).forward(request, response);
		}
	}
}
