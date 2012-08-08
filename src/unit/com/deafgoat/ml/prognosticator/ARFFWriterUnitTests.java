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
 * ARFFWriterTests.java 
 * Purpose: Unit tests for ARFFWriter
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;


public class ARFFWriterUnitTests {

	static final String	_config		= "weatherConfig.json";
	static ARFFWriter	_dump;
	static final String	_inputCsv	= "weather.csv";
	static final String	_targetArff	= "weather-dump.arff";

	@Before
	public final void testArffCreation() throws Exception {
		_dump = new ARFFWriter(new ConfigReader(_config), _inputCsv, _targetArff);
	}

	/**
	 * This will test that the ARFF file is actually created.
	 */
	@Test
	public final void testArffWrite() throws Exception {
		File arff = new File(_targetArff);
		if (arff.exists()) {
			assertTrue("ARFF could not be deleted", arff.delete());
		}
		_dump.writeARFF();
		assertTrue("ARFF not written!", arff.exists());

	}
}
