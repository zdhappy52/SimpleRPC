package net.sea.simple.rpc.register.center.zk.loadbalancer.strategy;

import net.sea.simple.rpc.utils.RPCServiceCache;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 轮询策略
 *
 * @author sea
 * @Date 2018/4/25 15:45
 * @Version 1.0
 */
public class ZKRoundLoadBalancerStrategy extends AbstractZKLoadBalancerStrategy {
    private Logger logger = Logger.getLogger(getClass());
    private static final String CURRENT_INDEX = "load_balancer_strategy_current_index";

    @Override
    protected synchronized String chooseNode(List<String> nodes) {
        Integer index = RPCServiceCache.newCache().getAttr(CURRENT_INDEX);
        if (index == null) {
            index = 0;
        } else {
            index++;
        }
        index = index % nodes.size();
        RPCServiceCache.newCache().putAttr(CURRENT_INDEX, index);
        logger.info(String.format("当前服务节点序号:%s", index));
        return nodes.get(index);
    }

    @Override
    protected boolean isLastNode(List<String> nodes) {
        return (Integer) RPCServiceCache.newCache().getAttr(CURRENT_INDEX) == nodes.size() - 1;
    }
}
