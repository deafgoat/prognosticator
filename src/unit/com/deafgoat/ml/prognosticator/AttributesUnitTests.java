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
 * AttributesTests.java 
 * Purpose: Unit tests for Attributes class
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;


public class AttributesUnitTests {

	@Test
	public final void testAttributeCreation() {
		Attributes att = Attributes.createAttribute("name", "", "string", false);
		assertNotNull(att);
	}

	@Test
	public final void testAttributeNameMutation() {
		Attributes att = Attributes.createAttribute("name", "", "string", false);
		assertEquals("name", att.getRawAttributeName());
		assertEquals("name", att.getAttributeName());
		att.setAttributeName("hello");
		assertEquals("hello", att.getAttributeName());
		assertEquals("name", att.getRawAttributeName());
	}

}
