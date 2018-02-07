/*
 * Copyright 2016-2017 Axioma srl.
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;

public class TestSampleData {

	public static final Date DATE1;
	public static final LocalDate LDATE1 = LocalDate.of(2018, Month.FEBRUARY, 7);
	
	public static final Date DATETIME1;
	public static final LocalDateTime LDATETIME1 = LocalDateTime.of(2018, Month.FEBRUARY, 7, 16, 30, 15);
	
	public static final LocalTime LTIME1 = LocalTime.of(18, 45, 30);
	
	public static final BigDecimal BD1 = BigDecimal.valueOf(145678,7632);
	
	static {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2018);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 7);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		DATE1 = c.getTime();
		
		c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2018);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 7);
		c.set(Calendar.HOUR_OF_DAY, 16);
		c.set(Calendar.MINUTE, 30);
		c.set(Calendar.SECOND, 15);
		c.set(Calendar.MILLISECOND, 0);
		DATETIME1 = c.getTime();
	}
	
}
