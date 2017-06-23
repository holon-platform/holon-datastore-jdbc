/*
 * Copyright 2000-2016 Holon TDCN.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.datastore.jdbc.test.data;

import java.io.Serializable;

public class TestProjectionBean implements Serializable {

	private static final long serialVersionUID = 7323766007354763956L;

	private long keycode;
	private String strv;

	public long getKeycode() {
		return keycode;
	}

	public void setKeycode(long keycode) {
		this.keycode = keycode;
	}

	public String getStrv() {
		return strv;
	}

	public void setStrv(String strv) {
		this.strv = strv;
	}

}
