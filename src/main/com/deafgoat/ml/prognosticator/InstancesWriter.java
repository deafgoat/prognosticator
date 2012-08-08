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

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * Utility to write Instances objects to ARFF.
 */
public class InstancesWriter {
	
	/**
	 * Saves an Instances object to .ARFF
	 * @param instances The set of instances to write
	 * @param outFile The file to which to write the instances
	 * @throws IOException If instances object can not be written
	 */
	public static void writeInstances(Instances instances, String outFile) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		saver.setFile(new File(outFile));
		saver.writeBatch();
	}
}
