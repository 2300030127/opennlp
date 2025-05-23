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

package opennlp.tools.cmdline.tokenizer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.cmdline.AbstractTrainerTool;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.cmdline.params.TrainingToolParams;
import opennlp.tools.cmdline.tokenizer.TokenizerTrainerTool.TrainerToolParams;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.ml.TrainerFactory;
import opennlp.tools.ml.TrainerFactory.TrainerType;
import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.tokenize.TokenizerFactory;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

public final class TokenizerTrainerTool
    extends AbstractTrainerTool<TokenSample, TrainerToolParams> {

  interface TrainerToolParams extends TrainingParams, TrainingToolParams {
  }

  public TokenizerTrainerTool() {
    super(TokenSample.class, TrainerToolParams.class);
  }

  @Override
  public String getShortDescription() {
    return "Trainer for the learnable tokenizer";
  }

  static Dictionary loadDict(File f) throws IOException {
    Dictionary dict = null;
    if (f != null && f.exists()) {
      CmdLineUtil.checkInputFile("abb dict", f);
      try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
        if (in.available() == 0) {
          throw new InvalidFormatException("Encountered an empty dictionary file?!");
        } else {
          dict = new Dictionary(in);
        }
      }
    }
    return dict;
  }

  @Override
  public void run(String format, String[] args) {
    super.run(format, args);
    if (null != params.getParams())
      mlParams = CmdLineUtil.loadTrainingParameters(params.getParams(), false);
    else
      mlParams = TrainingParameters.setParams(args);

    if (mlParams != null) {
      if (!TrainerFactory.isValid(mlParams)) {
        throw new TerminateToolException(1, "Training parameters file '" + params.getParams() +
            "' is invalid!");
      }

      if (!TrainerType.EVENT_MODEL_TRAINER.equals(TrainerFactory.getTrainerType(mlParams))) {
        throw new TerminateToolException(1, "Sequence training is not supported!");
      }
    }

    if (mlParams == null) {
      mlParams = ModelUtil.createDefaultTrainingParameters();
    }

    File modelOutFile = params.getModel();
    CmdLineUtil.checkOutputFile("tokenizer model", modelOutFile);

    TokenizerModel model;
    try {
      Dictionary dict = loadDict(params.getAbbDict());

      TokenizerFactory tokFactory = TokenizerFactory.create(
          params.getFactory(), params.getLang(), dict,
          params.getAlphaNumOpt(), null);
      model = opennlp.tools.tokenize.TokenizerME.train(sampleStream,
          tokFactory, mlParams);

    } catch (IOException e) {
      throw createTerminationIOException(e);
    }
    finally {
      try {
        sampleStream.close();
      } catch (IOException e) {
        // sorry that this can fail
      }
    }

    CmdLineUtil.writeModel("tokenizer", modelOutFile, model);
  }
}
