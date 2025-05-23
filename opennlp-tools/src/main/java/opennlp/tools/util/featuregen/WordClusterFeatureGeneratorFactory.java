/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.util.featuregen;

import java.util.HashMap;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.model.ArtifactSerializer;

/**
 * Defines a word cluster {@link GeneratorFactory}; it reads an element containing
 * 'w2vwordcluster' as a tag name. These clusters are typically produced by
 * word2vec or clark pos induction systems.
 *
 * @see WordClusterFeatureGenerator
 */
public class WordClusterFeatureGeneratorFactory
    extends GeneratorFactory.AbstractXmlFeatureGeneratorFactory {

  public WordClusterFeatureGeneratorFactory() {
    super();
  }

  @Override
  public AdaptiveFeatureGenerator create() throws InvalidFormatException {
    // if resourceManager is null, we don't instantiate
    if (resourceManager == null) {
      return null;
    }

    String dictResourceKey = getStr("dict");
    boolean lowerCaseDictionary = getBool("lowerCase");
    Object dictResource = resourceManager.getResource(dictResourceKey);
    if (!(dictResource instanceof WordClusterDictionary)) {
      throw new InvalidFormatException("Not a WordClusterDictionary resource for key: "
          + dictResourceKey);
    }

    return new WordClusterFeatureGenerator((WordClusterDictionary) dictResource,
        dictResourceKey, lowerCaseDictionary);
  }

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() throws InvalidFormatException {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put(getStr("dict"), new WordClusterDictionary.WordClusterDictionarySerializer());
    return mapping;
  }
}
