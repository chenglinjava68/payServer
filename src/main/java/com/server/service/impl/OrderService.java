package com.server.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.server.Config;
import com.server.db.dao.OrderRecordMapper;
import com.server.db.model.OrderRecord;
import com.server.db.model.OrderRecordExample;
import com.server.manager.OrderCacheMannager;
import com.server.model.DispatchStateEnum;
import com.server.model.OrderStateEnum;
import com.server.service.IOrderService;

/**
 * 
 * @author nullzZ
 *
 */
@Service
public class OrderService implements IOrderService {

    private static final Logger logger = Logger.getLogger(OrderService.class);
    @Resource
    private OrderRecordMapper orderRecordMapper;

    private long orderCount = 0;// 订单个数

    @Override
    public void loadOrderCount() {
	OrderRecordExample example = new OrderRecordExample();
	example.createCriteria().andUidIsNotNull();
	orderCount = orderRecordMapper.countByExample(example);
	logger.info("加载订单流水个数" + orderCount);
    }

    @Override
    public OrderRecord selectOrderRecord(long orderId) {
	OrderRecordExample example = new OrderRecordExample();
	example.createCriteria().andOrderIdEqualTo(orderId);
	List<OrderRecord> list = orderRecordMapper.selectByExample(example);
	if (list == null || list.size() <= 0) {
	    return null;
	} else {
	    return list.get(0);
	}
    }

    @Override
    public List<OrderRecord> selectUnDispathOrderDataList() {
	OrderRecordExample example = new OrderRecordExample();
	example.createCriteria().andDispatchStateEqualTo(DispatchStateEnum.READY.getType())
		.andOrderStateEqualTo(OrderStateEnum.SUCCESS.getType());
	return orderRecordMapper.selectByExample(example);
    }

    /**
     * 加载没有发货成功的订单
     */
    @Override
    public void loadUnDispathOrder() {
	List<OrderRecord> unDispathOrderDataList = selectUnDispathOrderDataList();
	for (OrderRecord data : unDispathOrderDataList) {
	    OrderCacheMannager.getReDispathOrderDataQueue().offer(data);
	}
    }

    @Override
    public int selectOrderCount(long orderId, String channelId) {
	OrderRecordExample example = new OrderRecordExample();
	example.createCriteria().andOrderIdEqualTo(orderId).andChannelIdEqualTo(channelId);
	return orderRecordMapper.countByExample(example);
    }

    @Override
    public List<OrderRecord> selectOrder(String serverId, String roleId, long startTime, long endTime) {
	OrderRecordExample example = new OrderRecordExample();
	example.createCriteria().andServerIdEqualTo(serverId).andRoleIdEqualTo(roleId).andCreateTimeBetween(startTime,
		endTime);
	return orderRecordMapper.selectByExample(example);
    }

    @Override
    public OrderRecord createOrder(String channelId, String serverId, long orderId, String productId, int productNum,
	    String roleId, String userId, long createTime, String ext, String orderInfo) {
	OrderRecord order = new OrderRecord();
	order.setChannelId(channelId);
	order.setCreateTime(createTime);
	order.setServerId(serverId);
	order.setOrderId(orderId);
	order.setProductId(productId);
	order.setProductNum(productNum);
	order.setOrderExt(ext);
	order.setRoleId(roleId);
	order.setUserId(userId);
	order.setOrderInfo(orderInfo);

	order.setDispatchState(0);
	order.setDispatchCount(0);
	order.setOrderState(DispatchStateEnum.READY.getType());

	if (!this.insertDB(order)) {
	    return null;
	}
	return order;
    }

    @Override
    public boolean insertDB(OrderRecord order) {
	return orderRecordMapper.insertSelective(order) > 0;
    }

    /**
     * 添加一个订单任务
     * 
     * @param order
     * @return
     */
    @Override
    public boolean offerNewOrder(OrderRecord order) {
	OrderCacheMannager.getNewOrderDataQueue().offer(order);
	logger.info("[发布订单]orderId:" + order.getOrderId());
	return false;
    }

    /**
     * 更新订单
     * 
     * @param order
     * @return
     */
    @Override
    public boolean updateOrder(OrderRecord order) {
	int ret = orderRecordMapper.updateByPrimaryKeySelective(order);
	if (ret > 0) {
	    return true;
	} else {
	    logger.error("[更新订单][异常]order:" + order.getOrderId());
	}
	return false;
    }

    @Override
    public void reDispath(OrderRecord order) {
	if (order != null) {
	    if (order.getDispatchCount() > Config.DispatchCount) {
		order.setDispatchState(DispatchStateEnum.FAIL.getType());// 彻底失败了
	    } else {
		order.setDispatchState(DispatchStateEnum.READY.getType());
		order.setDispatchCount(order.getDispatchCount() + 1);
		long d = System.currentTimeMillis() + Config.SOON_TIME;
		order.setNextDispatchTime(d);
		OrderCacheMannager.getReDispathOrderDataQueue().offer(order);
	    }
	}
    }

}
