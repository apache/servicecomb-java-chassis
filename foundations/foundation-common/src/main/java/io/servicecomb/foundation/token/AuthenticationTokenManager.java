package io.servicecomb.foundation.token;

public interface AuthenticationTokenManager {

	default public String getToken(){return "";};
	
	public boolean vaild(String token);
	
}
