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
 * CharterTests.java 
 * Purpose: Unit tests for Charter class
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;


public class CharterIntTests {

	static final String	_attribute	= "ChangeWindDirect";
	static final String	_config		= "src/example/com/deafgoat/ml/prognosticator/example/weather/weatherConfig.json";

	@Test
	public final void testCharter() throws Exception {
		Experimenter experimenter = new Experimenter(_config);
		String[] files = new String[] { experimenter._config._trueNegatives, experimenter._config._truePositives, experimenter._config._falseNegatives, experimenter._config._falsePositives };
		Charter charter = new Charter(experimenter);
		File png = new File(_attribute + ".png");
		if (png.exists()) {
			assertTrue("Image could not be deleted", png.delete());
		}
		charter.saveCategorical(_attribute, files);
		assertTrue("Image not saved properly", png.exists());
	}
}
