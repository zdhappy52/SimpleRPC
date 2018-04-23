package net.sea.simple.rpc.register;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkDataListener;
import com.github.zkclient.IZkStateListener;
import com.github.zkclient.ZkClient;
import net.sea.simple.rpc.exception.RPCServerRuntimeException;
import net.sea.simple.rpc.server.RegisterCenterConfig;
import net.sea.simple.rpc.server.ServiceInfo;
import net.sea.simple.rpc.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import java.util.List;

/**
 * 服务注册器
 *
 * @author sea
 */
public class ServiceRegister {
    //注册配置
    private static RegisterCenterConfig config;
    private Register REG_INST;

    public ServiceRegister(RegisterCenterConfig config) {
        ServiceRegister.config = config;
        REG_INST = Register.newInstance();
    }

    /**
     * 注册器
     *
     * @author sea
     */
    private static class Register {
        private Logger logger = Logger.getLogger(getClass());
        private volatile static Register register = null;
        private ZkClient client;
        private static final String ROOT_PATH = "/rpc/services/v_%s/";

        private Register() {
            init();
        }

        /**
         * 初始化方法
         */
        private void init() {
            client = new ZkClient(config.getZkServers(), config.getSessionTimeout(), config.getConnetionTimeout());
        }

        /**
         * 添加服务节点
         *
         * @param service
         * @return
         */
        public boolean addNode(ServiceInfo service) {
            if (service == null) {
                throw new RPCServerRuntimeException("无效的服务");
            }
            String serviceName = service.getServiceName();
            if (StringUtils.isBlank(serviceName)) {
                throw new IllegalArgumentException("无效的服务名");
            }
            String host = service.getHost();
            if (StringUtils.isBlank(host)) {
                throw new IllegalArgumentException("无效的服务主机");
            }
            // 创建以服务命名的服务节点(临时节点)
            String serviceNodePath = buildNodePath(service, host);
            client.createEphemeral(serviceNodePath, JsonUtils.toJson(service).getBytes());
            // 添加监听
            addWatcher(serviceNodePath);
            logger.info(String.format("注册服务：%s", service.toString()));
            return true;
        }

        /**
         * 构建服务节点
         *
         * @param service
         * @param host
         * @return
         */
        private String buildNodePath(ServiceInfo service, String host) {
            String namePath = getServiceNameNode(service);
            if (!client.exists(namePath)) {
                client.createPersistent(namePath, true);
            }
            return namePath.concat("/").concat(host);
        }

        /**
         * @param service
         * @return
         */
        public boolean reconnect(ServiceInfo service) {
            String path = buildNodePath(service, service.getHost());
            byte[] datas = JsonUtils.toJson(service).getBytes();
            if (client.exists(path)) {
                client.writeData(path, datas);
            } else {
                client.createEphemeral(path, datas);
            }
            logger.info(String.format("注册服务：%s", service.toString()));
            return true;
        }

        /**
         * 获取服务节点地址
         *
         * @param service
         * @return
         */
        private String getServiceNameNode(ServiceInfo service) {
            return String.format(ROOT_PATH.concat(service.getServiceName()), service.getVersion());
        }

        /**
         * 添加监听
         *
         * @param serviceNodePath
         */
        private void addWatcher(String serviceNodePath) {
            Watcher watcher = new Watcher();
            client.subscribeChildChanges("/rpc/services/v_1.0/net.sea.demo.service", watcher);
            client.subscribeDataChanges("/rpc/services/v_1.0/net.sea.demo.service", watcher);
            client.subscribeStateChanges(watcher);
        }

        /**
         * 删除服务节点
         *
         * @param service
         * @return
         */
        public boolean removeNode(ServiceInfo service) {
            String serviceName = service.getServiceName();
            if (StringUtils.isBlank(serviceName)) {
                return true;
            }
            String serviceNameNode = getServiceNameNode(service);
            String host = service.getHost();
            if (StringUtils.isBlank(host)) {
                return client.deleteRecursive(serviceNameNode);
            }
            return client.delete(serviceNameNode.concat("/").concat(host));
        }

        /**
         * 查找服务节点
         *
         * @param service
         * @return
         */
        public ServiceInfo findNode(ServiceInfo service) {
            return null;
        }

        /**
         * 服务节点是否存在
         *
         * @param service
         * @return
         */
        public boolean hasNode(ServiceInfo service) {
            String serviceName = service.getServiceName();
            if (StringUtils.isBlank(serviceName)) {
                return false;
            }
            String host = service.getHost();
            if (StringUtils.isBlank(host)) {
                return client.exists(getServiceNameNode(service));
            }
            return client.exists(getServiceNameNode(service).concat("/").concat(host));
        }

        /**
         * 创建实例
         *
         * @return
         */
        public static Register newInstance() {
            if (register == null) {
                synchronized (ServiceRegister.class) {
                    if (register == null) {
                        register = new Register();
                    }
                }
            }
            return register;
        }
    }

    /**
     * zk监听
     *
     * @author sea
     */
    private static class Watcher implements IZkChildListener, IZkStateListener, IZkDataListener {

        @Override
        public void handleDataChange(String dataPath, byte[] data) throws Exception {
            // TODO Auto-generated method stub
            System.out.println("dataPath = [" + dataPath + "], data = [" + data + "]");
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            // TODO Auto-generated method stub
            System.out.println("dataPath = [" + dataPath + "]");
        }

        @Override
        public void handleStateChanged(KeeperState state) throws Exception {
            // TODO Auto-generated method stub
            System.out.println("===========current state:" + state);
        }

        @Override
        public void handleNewSession() throws Exception {
            // TODO Auto-generated method stub
            System.out.println("===========handleNewSession");
        }

        @Override
        public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
            // TODO Auto-generated method stub
            System.out.println("parentPath = [" + parentPath + "], currentChildren = [" + currentChildren + "]");

        }

    }

    /**
     * 注册服务
     *
     * @param service
     * @return
     */
    public boolean register(ServiceInfo service) {
        return REG_INST.addNode(service);
    }

    /**
     * 重新注册服务
     *
     * @param service
     * @return
     */
    public boolean registerAgain(ServiceInfo service) {
        return REG_INST.reconnect(service);
    }

    /**
     * 根据服务名称注销服务
     *
     * @param serviceName
     * @return
     */
    public boolean unregister(String serviceName) {
        return unregister(new ServiceInfo(serviceName));
    }

    /**
     * 根据服务名称和地址注销服务
     *
     * @param serviceName
     * @param host
     * @return
     */
    public boolean unregister(String serviceName, String host) {
        return unregister(new ServiceInfo(serviceName, host));
    }

    /**
     * 注销服务
     *
     * @param service
     * @return
     */
    public boolean unregister(ServiceInfo service) {
        return REG_INST.removeNode(service);
    }

    /**
     * 根据服务名称查询RPC服务
     *
     * @param serviceName
     * @return
     */
    public ServiceInfo findService(String serviceName) {
        return findService(new ServiceInfo(serviceName));
    }

    /**
     * 根据服务名称和地址查询PRC服务
     *
     * @param serviceName
     * @param host
     * @return
     */
    public ServiceInfo findService(String serviceName, String host) {
        return findService(new ServiceInfo(serviceName, host));
    }

    /**
     * 查找RPC服务
     *
     * @param service
     * @return
     */
    public ServiceInfo findService(ServiceInfo service) {
        return REG_INST.findNode(service);
    }

    /**
     * 根据服务名称查询RPC服务是否存在
     *
     * @param serviceName
     * @return
     */
    public boolean hasService(String serviceName) {
        return hasService(new ServiceInfo(serviceName));
    }

    /**
     * 根据服务名称和地址查询PRC服务是否存在
     *
     * @param serviceName
     * @param host
     * @return
     */
    public boolean hasService(String serviceName, String host) {
        return hasService(new ServiceInfo(serviceName, host));
    }

    /**
     * 查询RPC服务是否存在
     *
     * @param service
     * @return
     */
    public boolean hasService(ServiceInfo service) {
        return REG_INST.hasNode(service);
    }
}