package com.daimler.sechub.adapter;

import javax.crypto.SealedObject;

public class BasicLoginConfig implements LoginConfig{

	SealedObject realm;
	SealedObject user;
	SealedObject password;

	public String getRealm() {
		return decrypt(realm);
	}

	public String getUser() {
		return decrypt(user);
	}

	public String getPassword() {
		return decrypt(password);
	}

}