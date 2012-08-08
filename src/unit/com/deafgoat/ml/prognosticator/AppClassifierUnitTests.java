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
 * AppClassifierTests.java 
 * Purpose: Unit tests for AppClassifier
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import weka.core.Instances;


public class AppClassifierUnitTests {

	static AppClassifier	_classifier;
	static final String		_config		= "weatherConfig.json";
	static final String		_inputARFF	= "weather-dump.arff";
	static Instances		_instances;

	@Test
	public final void testClassifier() throws Exception {
		_instances = new InstancesReader(_inputARFF).readFromARFF();
		_classifier = new AppClassifier(_instances, new ConfigReader(_config));
		assertNotNull("Could not create classifier", _classifier);
	}
}
