package logic.bean;

import java.io.Serializable;

import logic.exception.SyntaxException;
import logic.util.Util;

public class UserAuthBean implements Serializable {
	private static final long serialVersionUID = 6053272591238153999L;
	
	private String email;
	private String password;

	public String getEmail() {
		return this.email;
	}

	public String getPassword() {
		return password;
	}

	public void setEmail(String email) 
			throws SyntaxException {
		if(email.length() > 255) {
			throw new SyntaxException("Email length must be less than 255 chars!");
		}

		if(!Util.isValidEmail(email)) {
			throw new SyntaxException("Could not recognize email pattern!");
		}

		this.email = email;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}