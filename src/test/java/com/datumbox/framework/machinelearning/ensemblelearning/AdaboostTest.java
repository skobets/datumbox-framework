/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.machinelearning.ensemblelearning;

import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.Configuration;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.framework.machinelearning.classification.MultinomialNaiveBayes;
import com.datumbox.tests.TestConfiguration;
import com.datumbox.tests.abstracts.AbstractTest;
import com.datumbox.tests.Datasets;
import com.datumbox.tests.utilities.TestUtils;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for Adaboost.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class AdaboostTest extends AbstractTest {

    /**
     * Test of validate method, of class Adaboost.
     */
    @Test
    public void testValidate() {
        logger.info("validate");
        
        Configuration conf = TestUtils.getConfig();
        
        
        Dataframe[] data = Datasets.carsCategorical(conf);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        
        String dbName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, conf);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        
        df.transform(validationData);
        
        
        Adaboost instance = new Adaboost(dbName, conf);
        
        Adaboost.TrainingParameters param = new Adaboost.TrainingParameters();
        param.setMaxWeakClassifiers(5);
        param.setWeakClassifierClass(MultinomialNaiveBayes.class);
        
        
        MultinomialNaiveBayes.TrainingParameters trainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        trainingParameters.setMultiProbabilityWeighted(true);
        
        
        param.setWeakClassifierTrainingParameters(trainingParameters);
        
        
        instance.fit(trainingData, param);
        
        
        instance.close();
        df.close();
        //instance = null;
        //df = null;
        
        df = new DummyXYMinMaxNormalizer(dbName, conf);
        instance = new Adaboost(dbName, conf);
        
        instance.validate(validationData);
        
        
        
        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        for(Map.Entry<Integer, Record> e : validationData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            expResult.put(rId, r.getY());
            result.put(rId, r.getYPredicted());
        }
        assertEquals(expResult, result);
        
        df.delete();
        instance.delete();
        
        trainingData.delete();
        validationData.delete();
    }
    

    /**
     * Test of kFoldCrossValidation method, of class Adaboost.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("kFoldCrossValidation");
        
        Configuration conf = TestUtils.getConfig();
        
        int k = 5;
        
        Dataframe[] data = Datasets.carsNumeric(conf);
        Dataframe trainingData = data[0];
        data[1].delete();
        
        
        String dbName = this.getClass().getSimpleName();
        Adaboost instance = new Adaboost(dbName, conf);
        
        Adaboost.TrainingParameters param = new Adaboost.TrainingParameters();
        param.setMaxWeakClassifiers(5);
        param.setWeakClassifierClass(MultinomialNaiveBayes.class);
        
        
        MultinomialNaiveBayes.TrainingParameters trainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        trainingParameters.setMultiProbabilityWeighted(true);
        
        param.setWeakClassifierTrainingParameters(trainingParameters);

        
        Adaboost.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);
        
        double expResult = 0.6923992673992675;
        double result = vm.getMacroF1();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        instance.delete();
        
        trainingData.delete();
    }
    
}
