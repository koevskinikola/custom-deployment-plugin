/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.plugin.customdeploymentplugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;

public class CustomDeploymentPluginTest {

  private static ProcessEngineConfiguration CONFIGURATION = new StandaloneInMemProcessEngineConfiguration() {
    {
      this.getProcessEnginePlugins().add(new CustomDeploymentPlugin());
    }
  };

  private static ProcessEngine processEngine;

  public static ProcessEngine processEngine() {

    if (processEngine == null) {
      processEngine = CONFIGURATION.buildProcessEngine();
    }

    return processEngine;
  }

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule(processEngine());

  @Test
  public void testPluginPresence() {
    //given
    List<ProcessEnginePlugin> plugins =
        ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration())
            .getProcessEnginePlugins();

    // when

    // then
    assertThat(plugins).extracting("class")
                       .containsOnlyOnce(CustomDeploymentPlugin.class);
  }


  @Test
  public void testDeployment() {
    // given
    BpmnModelInstance process712 = Bpmn.createExecutableProcess("process")
        .camundaVersionTag("process712")
        .startEvent("start")
        .endEvent("end")
        .done();

    BpmnModelInstance process713 = Bpmn.createExecutableProcess("process")
       .camundaVersionTag("process713")
       .startEvent("start")
       .endEvent("end")
       .done();

    Deployment deployment712 = processEngine.getRepositoryService().createDeployment()
       .enableDuplicateFiltering(true)
       .name("test")
       .addModelInstance("process.bpmn", process712)
       .deploy();

    // when
    Deployment deployment713 = processEngine.getRepositoryService().createDeployment()
        .enableDuplicateFiltering(true)
        .name("test")
        .addModelInstance("process.bpmn", process713)
        .deploy();

    // then
    List<Resource> resources = processEngine.getRepositoryService()
        .getDeploymentResources(deployment713.getId());
    assertThat(deployment713.getId()).isEqualTo(deployment712.getId());
  }
}