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

package com.deafgoat.ml.prognosticator.example.weather;

// Prognosticator
import com.deafgoat.ml.prognosticator.Experimenter;
import com.deafgoat.ml.prognosticator.MongoExport;
import com.deafgoat.ml.prognosticator.MongoImport;

public class WeatherTomorrowPredictor {
	static final String	_collection	= "configuration";
	static final String	_config		= "src/example/com/deafgoat/ml/prognosticator/example/weather/weatherConfig.json";
	static final String	_database	= "weather";
	static final String	_host		= "localhost";
	static Experimenter	_experimenter;
	static String[]		_json;
	static MongoExport	_mongoExport;
	static MongoImport	_mongoImport;
	
    /**
     * This runs the training and analysis.
     */
    public static void main(final String [] pArgs) throws Exception {

        // Create experimenter object.
    	_experimenter = new Experimenter(_config);
    		
    	// Use configurations object in config file
    	
        // Build the model.
        _experimenter.buildModel();

        // Run the prediction
        _experimenter.predict();
    }
   
}