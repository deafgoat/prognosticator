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
 * DataTests.java 
 * Purpose: Unit tests for data reader/filter classes
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import weka.core.Instances;


public class DataUnitTests {

	static ARFFWriter	_dump;
	static final String	_filter		= "nominal";
	static Instances	_instances;
	static InstancesReader	_reader;
	static final String	_targetArff	= "weather-dump.arff";

	@Before
	public final void ensure() {
		File arff = new File(_targetArff);
		assertTrue("Could not find ARFF file", arff.exists());
	}

	@Test
	public final void initializeReader() throws Exception {
		_reader = new InstancesReader(_targetArff);
		assertNotNull("DataReader could not be initialized!", _reader);
	}

	@Test
	public final void readInstances() throws Exception {
		_instances = _reader.readFromARFF();
		assertNotNull("DataReader could not be initialized!", _instances);
	}

	@Test
	public final void testDataFilter() throws Exception {
		int total = _instances.numAttributes();
		int size = _instances.size();
		int filterCount = 0;
		for (int i = 0; i < total; i++) {
			if (_instances.attribute(i).equals(_filter)) {
				filterCount += 1;
			}
		}
		InstancesFilter df = new InstancesFilter(_instances);
		df.removeTypeFilter(new String[] { _filter });
		assertEquals("Number of instances changed!", size, _instances.size());
		assertEquals("Unexpected number of attributes!", _instances.numAttributes(), (total - filterCount));
	}
}
