/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.module.Run;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.perfmon.IPktInProcessingTimeService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.threadpool.IThreadPoolService;

import org.sdnplatform.sync.ISyncService;
/**
 * 两个主要功能
 * 1 处理交换机之间的链接并将openflow消息转化为其他模块可以监听的事件
 * 2 它决定某些特定的openflow消息（即packetin、FlowRemoved、PortStatus等）
 * 被分配到该侦听消息的模块顺序
 * @author wzc
 *floodlightProvider使用Netty库来处理交换机的线程和链接，
 *每一个openflow消息将通过一个Netty线程来处理，并执行与所有模块的消息相关联的所有逻辑
 */
public class FloodlightProvider implements IFloodlightModule {
    Controller controller;

    public FloodlightProvider() {
        controller = new Controller();
    }
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> services =
                new ArrayList<Class<? extends IFloodlightService>>(1);
        services.add(IFloodlightProviderService.class);
        return services;
    }

    @Override
    public Map<Class<? extends IFloodlightService>,
               IFloodlightService> getServiceImpls() {
        controller = new Controller();

        Map<Class<? extends IFloodlightService>,
            IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>,
                            IFloodlightService>();
        m.put(IFloodlightProviderService.class, controller);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> dependencies =
            new ArrayList<Class<? extends IFloodlightService>>(4);
        dependencies.add(IStorageSourceService.class);
        dependencies.add(IPktInProcessingTimeService.class);
        dependencies.add(IRestApiService.class);
        dependencies.add(IDebugCounterService.class);
        dependencies.add(IOFSwitchService.class);
        dependencies.add(IThreadPoolService.class);
        dependencies.add(ISyncService.class);
        return dependencies;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
       controller.setStorageSourceService(
           context.getServiceImpl(IStorageSourceService.class));
       controller.setPktInProcessingService(
           context.getServiceImpl(IPktInProcessingTimeService.class));
       controller.setDebugCounter(
           context.getServiceImpl(IDebugCounterService.class));
       controller.setRestApiService(
           context.getServiceImpl(IRestApiService.class));
       controller.setThreadPoolService(
           context.getServiceImpl(IThreadPoolService.class));
       controller.setSyncService(
           context.getServiceImpl(ISyncService.class));
       controller.setSwitchService(
    	   context.getServiceImpl(IOFSwitchService.class));
       controller.init(context.getConfigParams(this));
    }

    @Override
    public void startUp(FloodlightModuleContext context)
            throws FloodlightModuleException {
        controller.startupComponents(context.getModuleLoader());
    }
    
    @Run(mainLoop=true)
    public void run() throws FloodlightModuleException {
        controller.run();
    }
}
