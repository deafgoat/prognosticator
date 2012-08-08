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
 */

package com.deafgoat.ml.prognosticator;

// Java
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// Weka
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Utility to read from several data sources. This class provides an interface
 * to read from ARFF, CSV, and MongoDB. For MongoDB, it works with simple
 * collections with no nested fields.
 */
public class InstancesReader {
	
	/**
	 * @return all instances read from the data source
	 */
	public Instances getInstances() {
		return _instances;
	}

	/**
	 * Read from a .ARFF file
	 * @throws IOException If the ARFF file can not be found
	 * @return the set of instances read
	 */
	public Instances readFromARFF() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(_filename));
		_instances = new Instances(reader);
		reader.close();
		return _instances;
	}

	/**
	 * Read from a .CSV file
	 * @throws IOException
	 * @return the set of instances read
	 */
	public Instances readFromCSV() throws IOException {
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(_filename));
		_instances = loader.getDataSet();
		return _instances;
	}

	/**
	 * Read directly from a MongoDB collection
	 * @param csv The CSV file to read into
	 * @throws Exception If collection can not be read
	 * @return the set of instances read
	 */
	public Instances readFromMongoDB(String csv) throws Exception {
		_m2v = new Mongo2CSV(_host, _port, _db, _coll);
		_m2v.setFields(_fields);
		_m2v.writeCSV(csv);
		_m2v.close();
		_source = new DataSource(csv);
		_instances = _source.getDataSet();
		return _instances;
	}

	/**
	 * working collection
	 */
	private String _coll;
	
	/**
	 * working database
	 */
	private String _db;
	
	/**
	 * working host
	 */
	private String _host;
	
	/**
	 * working instances to read to 
	 */
	private Instances _instances;
	
	/**
	 * working port
	 */
	private Integer _port;

	/**
	 * sets of fields to read
	 */
	private String[] _fields;

	/**
	 * the file to write to
	 */
	private String _filename;

	/**
	 * Mongo2CSV handle
	 */
	private Mongo2CSV _m2v;

	/**
	 * handler for data source
	 */
	private DataSource _source;

	/**
	 * @param filename The source file to read from
	 */
	public InstancesReader(String filename) {
		_filename = filename;
	}

	/**
	 * @param host The hostname of the database
	 * @param port The port number of the database
	 * @param db The name of the database
	 * @param coll The name of the collection
	 * @param fields The set of fields to read from the collection
	 */
	public InstancesReader(String host, Integer port, String db, String coll, String[] fields) {
		_host = host;
		_port = port;
		_db = db;
		_coll = coll;
		_fields = fields;
	}

}
