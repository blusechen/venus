package com.meidusa.venus.http.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.meidusa.venus.util.ThreadLocalMap;
import com.meidusa.venus.util.VenusTracerUtil;

public class VenusTracerCleanFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try{
			if(VenusTracerUtil.getTracerID() == null){
				VenusTracerUtil.randomTracerID();
			}
			chain.doFilter(request, response);
		}finally{
			ThreadLocalMap.remove(VenusTracerUtil.REQUEST_TRACE_ID);
		}
	}

	@Override
	public void destroy() {

	}

}
