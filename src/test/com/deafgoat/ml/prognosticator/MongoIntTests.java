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
 * MongoTests.java 
 * Purpose: Integration tests for Mongo classes
 * 
 */

package com.deafgoat.ml.prognosticator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;

public class MongoIntTests {

	static final String		_collection	= "configuration";
	static final String		_database	= "weather";
	static ARFFWriter		_dump;
	static final String[]	_fields		= new String[] { "name", "dumpARFF" };
	static final String		_host		= "localhost";
	static JSONObject[]		_json;
	static final String		_jsonStr	= "{\"name\":\"weather\",\"dumpARFF\":\"weather-dump.arff\",\"data\":{\"dateFormat\":\"yyyy-MM-dd\",\"classValue\":\"RainTomorrow\",\"positiveClassValue\":\"1\",\"negativeClassValue\":\"0\",\"positiveClassWeight\":1,\"negativeClassWeight\":1},\"errorAnalysis\":{\"truePositives\":\"tp.txt\",\"trueNegatives\":\"tn.txt\",\"falsePositives\":\"fp.txt\",\"falseNegatives\":\"fn.txt\"},\"classifier\":{\"model\":{\"classifier\":\"LogitBoost\",\"outputModel\":\"classifier.model\"},\"crossValidation\":{\"numFolds\":10,\"arguments\":\"\",\"seed\":10},\"classify\":{\"testARFF\":\"weather-test.arff\",\"trainingARFF\":\"weather-training.arff\"},\"prediction\":{\"maxCount\":0,\"minProb\":0,\"file\":\"weather.tsv\"},\"writeToMongoDB\":false,\"writeToFile\":true},\"mongoDB\":{\"hostname\":\"localhost\",\"port\":27017,\"database\":\"weather\",\"configCollection\":\"config\",\"modelCollection\":\"experiments\",\"predictionCollection\":\"predictions\"},\"dump\":{\"file\":\"weather.csv\",\"attributes\":[{\"rawAttributeName\":\"Date\",\"attributeName\":\"\",\"attributeType\":\"nominal\",\"include\":true}]}}";
	static MongoExport		_mongoExport;
	static MongoImport		_mongoImport;
	static final int		_port		= 27017;
	static final String		_targetCsv	= "test.csv";

	@Before
	public final void initMongoExport() throws FileNotFoundException, IOException, JSONException {
		_json = new JSONObject[] { new JSONObject(_jsonStr) };
		_mongoExport = new MongoExport(_json, _host, _port, _database, _collection);
	}

	@Before
	public final void initMongoImport() throws FileNotFoundException, IOException, JSONException {
		_mongoImport = new MongoImport(_host, _port, _database, _collection);
	}

	@Test
	public final void testMongoExport() throws JSONException, FileNotFoundException, MongoException, IOException {
		_mongoExport.writeJSON();
		JSONObject configuration = _mongoImport.getConfiguration("weather");
		assertNotNull("Configuration not found", configuration);
	}

	@Test
	public final void testImportContent() throws JSONException, FileNotFoundException, MongoException, IOException {
		JSONObject configuration = _mongoImport.getConfiguration("weather");
		assertTrue("Name mismatch!", configuration.getString("dumpARFF").equals("weather-dump.arff"));
	}

	@Test
	public final void testMongo2CSV() throws UnknownHostException, IOException {
		Mongo2CSV x = new Mongo2CSV(_host, _port, _database, _collection);
		File csv = new File(_targetCsv);
		x.setFields(_fields);
		if (csv.exists()) {
			assertTrue("CSV could not be deleted", csv.delete());
		}
		x.writeCSV(_targetCsv);
		assertTrue("CSV was not written!", csv.exists());
	}

	@Test
	public final void testPreConfigurationCount() throws JSONException, FileNotFoundException, MongoException, IOException {
		_mongoExport.writeJSON();
		ArrayList<JSONObject> configurations = _mongoImport.getAllConfigurations();
		assertTrue("Configuration count not expected", configurations.size() == 1);
	}

	@Test
	public final void testRemoveContent() throws JSONException, FileNotFoundException, MongoException, IOException {
		assertTrue("Could not remove configuration!", _mongoImport.removeConfiguration("weather"));
	}

	@Test
	public final void testPostConfigurationCount() throws JSONException, FileNotFoundException, MongoException, IOException {
		ArrayList<JSONObject> configurations = _mongoImport.getAllConfigurations();
		assertTrue("Configuration count not expected", configurations.size() == 0);
	}

}
