package com.server.action;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.server.ChannelEnum;
import com.server.core.service.impl.OrderService;
import com.server.core.util.HttpUtil;

/**
 * 
 * @author nullzZ
 *
 */
@Controller
@RequestMapping(value = "/s")
public class PayAction {

    // private static final Logger logger = Logger.getLogger(PayAction.class);
    @Resource
    private OrderService orderService;

    @RequestMapping(value = "/payProductId", method = RequestMethod.POST)
    public void clientReCall(HttpServletRequest request, HttpServletResponse response) {
	String sdk = request.getParameter("sdk");
	String channelId = request.getParameter("channelId");
	String serverId = request.getParameter("serverId");
	String roleId = request.getParameter("roleId");
	String userId = request.getParameter("userId");
	String productId = request.getParameter("productId");

	boolean ret = orderService.compensateOrder(ChannelEnum.get(Integer.parseInt(sdk)), channelId, serverId, roleId,
		userId, productId);
	if (ret) {
	    HttpUtil.write(response, "ok");
	} else {
	    HttpUtil.write(response, "fail");
	}
    }

}
