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
 * ExperimenterTests.java 
 * Purpose: Integration tests for experimenter
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;


public class ExperimenterIntTests {

	static final String	_config	= "weatherConfig.json";
	static Experimenter	_experimenter;

	@Before
	public final void initialize() throws Exception {
		_experimenter = new Experimenter("weatherConfig.json");
	}

	@Test
	public final void testBuildModel() throws Exception {
		File model = new File(_experimenter._config._modelFile);
		if (model.exists()) {
			assertTrue("Model could not be deleted", model.delete());
		}
		_experimenter.buildModel();
		assertTrue("Model not written!", model.exists());
	}

	@Test
	public final void testPrediction() throws Exception {
		File prediction = new File(_experimenter._config._predictionFile);
		if (prediction.exists()) {
			assertTrue("Prediction could not be deleted", prediction.delete());
		}
		_experimenter.predict();
		assertTrue("Prediction not written!", prediction.exists());
	}
}
