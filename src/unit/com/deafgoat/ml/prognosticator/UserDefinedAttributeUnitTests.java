/**
 * Copyright 2012, Wisdom Omuya.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * UserDefinedAttributeTests.java
 * Purpose: Shows how users can implement their own features. This class
 * computes the change in temperature from 9am to 3pm on that day.
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;


public final class UserDefinedAttributeUnitTests extends UserDefinedAttribute {

	static Attributes	_attribute;

	@Before
	public final void createAttribute() throws Exception {
		_attribute = Attributes.createAttribute("9AM_to_3PM_temp_change", "9AM_to_3PM_temp_change", "nominal", true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.deafgoat.ml.prognosticator.UserDefinedAttribute#getAttributeName()
	 */
	@Override
	public String getAttributeName() {
		return UserDefinedAttributeUnitTests._attribute.getAttributeName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.deafgoat.ml.user.UserDefinedAttribute#getAttributeValue
	 * (java.util.List, java.util.Map)
	 */
	@Override
	public String getAttributeValue(List<String> instance, Map<String, Integer> attributeMap, ConfigReader cfg) {
		String str3PMTemp = instance.get(attributeMap.get("Temp3pm"));
		String str9AMTemp = instance.get(attributeMap.get("Temp9am"));
		Double diffTemp = Double.parseDouble(str3PMTemp) - Double.parseDouble(str9AMTemp);
		NumberFormat formatter = new DecimalFormat("#0.00");
		return formatter.format(diffTemp);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.deafgoat.ml.prognosticator.UserDefinedAttribute#getAttributeName()
	 * getUserDefinedAttributes()
	 */
	@Override
	public Attributes getUserDefinedAttribute() {
		return _attribute;
	}

	@Test
	public final void testAttributeCreation() throws Exception {
		createAttribute();
		assertNotNull("Could not create attribute", getUserDefinedAttribute());
	}

	@Test
	public final void testGetAttributeValue() {
		List<String> instance = new ArrayList<String>();
		Map<String, Integer> attributeMap = new HashMap<String, Integer>();
		ConfigReader cfgRdr = new ConfigReader(new JSONObject());
		instance.add("26");
		instance.add("24");
		attributeMap.put("Temp3pm", 0);
		attributeMap.put("Temp9am", 1);
		assertEquals("Incorrect attribute value returned", "2.00", getAttributeValue(instance, attributeMap, cfgRdr));
	}

}
