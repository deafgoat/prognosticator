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
 * MongoExperimenterTests.java 
 * Purpose: Integration tests for experimenter and mongodb
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;


public class MongoExperimenterIntTests {

	static final String	_collection	= "configuration";
	static final String	_config		= "weatherConfig.json";
	static final String	_database	= "weather";
	static Experimenter	_experimenter;
	static final String	_host		= "localhost";
	static String[]		_json;
	static MongoExport	_mongoExport;
	static MongoImport	_mongoImport;
	static final int	_port		= 27017;

	@Before
	public final void doMongoExport() throws FileNotFoundException, IOException, JSONException {
		initMongoExport();
		_mongoExport.writeJSON();
	}

	@Before
	public final void initExperimenter() throws Exception {
		_experimenter = new Experimenter(_config);
	}

	@Before
	public final void initMongoExport() throws FileNotFoundException, IOException, JSONException {
		_json = new String[] { _config };
		_mongoExport = new MongoExport(_json, _host, _port, _database, _collection);
	}

	@Before
	public final void initMongoImport() throws FileNotFoundException, IOException, JSONException {
		_mongoImport = new MongoImport(_host, _port, _database, _collection);
	}

	@Test
	public final void testExperimenter() throws Exception {
		File model, prediction;
		ArrayList<JSONObject> configuration = _mongoImport.getAllConfigurations();
		for (int i = 0; i < configuration.size(); i++) {
			prediction = new File(_experimenter._config._predictionFile);
			model = new File(_experimenter._config._modelFile);
			if (model.exists()) {
				assertTrue("Model could not be deleted", model.delete());
			}
			_experimenter.buildModel();
			assertTrue("Model not written!", model.exists());
			if (prediction.exists()) {
				assertTrue("Prediction could not be deleted", prediction.delete());
			}
			_experimenter.predict();
			assertTrue("Prediction not written!", prediction.exists());
		}
	}

	@Test
	public final void removeAllConfig() throws FileNotFoundException, IOException, JSONException {
		initMongoImport();
		ArrayList<JSONObject> configuration = _mongoImport.getAllConfigurations();
		for (int i = 0; i < configuration.size(); i++) {
			assertTrue("Could not remove configuration!", _mongoImport.removeConfiguration(configuration.get(i).getString("name")));
		}
	}

}
