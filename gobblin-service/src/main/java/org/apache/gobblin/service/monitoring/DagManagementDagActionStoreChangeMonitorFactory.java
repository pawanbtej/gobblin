/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.gobblin.service.monitoring;

import java.util.Objects;

import com.typesafe.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import lombok.extern.slf4j.Slf4j;

import org.apache.gobblin.runtime.spec_catalog.FlowCatalog;
import org.apache.gobblin.runtime.util.InjectionNames;
import org.apache.gobblin.service.modules.orchestration.DagActionReminderScheduler;
import org.apache.gobblin.service.modules.orchestration.DagManagement;
import org.apache.gobblin.service.modules.orchestration.DagManagementStateStore;
import org.apache.gobblin.service.modules.orchestration.DagManager;
import org.apache.gobblin.service.modules.orchestration.Orchestrator;
import org.apache.gobblin.util.ConfigUtils;


/**
 * A factory implementation that returns a {@link DagManagementDagActionStoreChangeMonitor} instance.
 */
@Slf4j
public class DagManagementDagActionStoreChangeMonitorFactory implements Provider<DagActionStoreChangeMonitor> {
  static final String DAG_ACTION_STORE_CHANGE_MONITOR_NUM_THREADS_KEY = "numThreads";

  private final Config config;
  private final FlowCatalog flowCatalog;
  private final Orchestrator orchestrator;
  private final DagManagementStateStore dagManagementStateStore;
  private final boolean isMultiActiveSchedulerEnabled;
  private final DagManagement dagManagement;
  private final DagActionReminderScheduler dagActionReminderScheduler;

  @Inject
  public DagManagementDagActionStoreChangeMonitorFactory(Config config, DagManager dagManager, FlowCatalog flowCatalog,
      Orchestrator orchestrator, DagManagementStateStore dagManagementStateStore, DagManagement dagManagement,
      @Named(InjectionNames.MULTI_ACTIVE_SCHEDULER_ENABLED) boolean isMultiActiveSchedulerEnabled,
      DagActionReminderScheduler dagActionReminderScheduler) {
    this.config = Objects.requireNonNull(config);
    this.flowCatalog = flowCatalog;
    this.orchestrator = orchestrator;
    this.dagManagementStateStore = dagManagementStateStore;
    this.isMultiActiveSchedulerEnabled = isMultiActiveSchedulerEnabled;
    this.dagManagement = dagManagement;
    this.dagActionReminderScheduler = dagActionReminderScheduler;
  }

  private DagManagementDagActionStoreChangeMonitor createDagActionStoreMonitor() {
    Config dagActionStoreChangeConfig = this.config.getConfig(DagActionStoreChangeMonitor.DAG_ACTION_CHANGE_MONITOR_PREFIX);
    log.info("DagActionStore will be initialized with config {}", dagActionStoreChangeConfig);

    int numThreads = ConfigUtils.getInt(dagActionStoreChangeConfig, DAG_ACTION_STORE_CHANGE_MONITOR_NUM_THREADS_KEY, 5);

    return new DagManagementDagActionStoreChangeMonitor(dagActionStoreChangeConfig,
        numThreads, flowCatalog, orchestrator, dagManagementStateStore, isMultiActiveSchedulerEnabled, this.dagManagement,
        this.dagActionReminderScheduler);
  }

  @Override
  public DagActionStoreChangeMonitor get() {
    DagActionStoreChangeMonitor changeMonitor = createDagActionStoreMonitor();
    return changeMonitor;
  }
}
