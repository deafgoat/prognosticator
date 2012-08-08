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

package com.deafgoat.ml.prognosticator.example.stock;

// Prognosticator
import com.deafgoat.ml.prognosticator.ARFFWriter;
import com.deafgoat.ml.prognosticator.ConfigReader;
import com.deafgoat.ml.prognosticator.Experimenter;

/**
 * This is a simple example of analytzing a security for a brief period of time.
 */
public class AppleStockPricePredictor {

    private static final String TRAINING_CSV = "src/example/com/deafgoat/ml/prognosticator/example/stock/train.csv";
    private static final String TEST_CSV = "src/example/com/deafgoat/ml/prognosticator/example/stock/test.csv";

    private static final String TRAINING_ARFF = "src/example/com/deafgoat/ml/prognosticator/example/stock/apple-stock-price-train.arff";
    private static final String TEST_ARFF = "src/example/com/deafgoat/ml/prognosticator/example/stock/apple-stock-price-test.arff";

    private static final String CONFIG_FILE = "src/example/com/deafgoat/ml/prognosticator/example/stock/config.json";

    /**
     * This runs the training and analysis.
     */
    public static void main(final String [] pArgs) throws Exception {

        final ConfigReader config = new ConfigReader(CONFIG_FILE);

	    final ARFFWriter testArffWriter = new ARFFWriter(config, TEST_CSV, TEST_ARFF);
        testArffWriter.writeARFF();

	    final ARFFWriter trainingArffWriter = new ARFFWriter(config, TRAINING_CSV, TRAINING_ARFF);
        trainingArffWriter.writeARFF();

        // Create experimenter object.
        final Experimenter experimenter = new Experimenter(CONFIG_FILE);

        // Build the model.
        experimenter.buildModel();

        // Run the prediction
        experimenter.predict();

    }
}

