package com.server.manager;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.server.ChannelEnum;
import com.server.model.IDispathHandle;

/**
 * @author nullzZ
 * 
 */
public class DispathHandleManager {

    private static final Logger logger = Logger.getLogger(DispathHandleManager.class);
    private ConcurrentHashMap<Integer, IDispathHandle> runnables = new ConcurrentHashMap<>();
    private static final DispathHandleManager instance = new DispathHandleManager();

    private DispathHandleManager() {

    }

    public static DispathHandleManager getInstance() {
	return instance;
    }

    public void register(ChannelEnum channelEnum, IDispathHandle r) {
	this.runnables.put(channelEnum.value, r);
	logger.info("[注册处理类]channel:" + channelEnum.value);
    }

    public IDispathHandle get(int channelEnumValue) {
	return this.runnables.get(channelEnumValue);
    }
}
