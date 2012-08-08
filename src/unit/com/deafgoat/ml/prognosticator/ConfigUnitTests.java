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
 * ConfigTests.java 
 * Purpose: Unit tests for ConfigReader class.
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;


public class ConfigUnitTests {

	static final String	_config	= "weatherConfig.json";
	static ConfigReader	_configReader;

	@Test
	public final void configTests() throws IOException, JSONException {
		_configReader.readConfig();
		assertTrue("Name mismatch!", _configReader._relation.equals("weather-configuration"));
	}

	@Before
	public final void initializeConfig() throws IOException, JSONException {
		_configReader = new ConfigReader(_config);
		assertNotNull("Could not initialize ConfigReader", _configReader);
	}

}
