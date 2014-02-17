package net.rayherring;

import java.util.HashMap;

public class OnlinePlayersSQLQuery {
	String query;
	HashMap<Integer,Object> queryParams = new HashMap<Integer,Object>();
	int numberOfParameters;
	
	public OnlinePlayersSQLQuery(String query, Object...params) {
		this.query = query;
		this.numberOfParameters = params.length;
		
		for ( int i = 0; i < params.length; i++ ) {
			if ( params[i] instanceof Integer ) queryParams.put(i, (int) params[i]);
			if ( params[i] instanceof String ) queryParams.put(i, (String) params[i]);
			if ( params[i] instanceof Boolean ) queryParams.put(i, (boolean) params[i]);
		}
	}
	
	public String getQuery() {
		return query;
	}
	
	public int numberOfParams() {
		return numberOfParameters;
	}

	public HashMap getParams() {
		return queryParams;
	}

}
