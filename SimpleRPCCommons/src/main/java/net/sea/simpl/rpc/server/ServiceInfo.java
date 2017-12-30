package net.sea.simpl.rpc.server;

import net.sea.simpl.rpc.constants.CommonConstants;

/**
 * 服务信息对象
 * 
 * @author sea
 *
 */
public class ServiceInfo {
	private String serviceName;// 服务名称
	private String host;// 服务器地址
	private int port;// 服务端口
	private int maxConnections = CommonConstants.DEFAULT_MAX_CONNECTIONS;// 最大连接数
	private int currentConnections;// 当前连接数
	private float weight;// 服务负载
	private ServiceChoiceStrategy strategy;// 服务选择策略
	private String version = CommonConstants.DEFAULT_SERVICE_VERSION;// 服务版本号

	public ServiceInfo(String serviceName) {
		super();
		this.serviceName = serviceName;
	}

	public ServiceInfo(int port) {
		super();
		this.port = port;
	}

	public ServiceInfo(String serviceName, String host) {
		super();
		this.serviceName = serviceName;
		this.host = host;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public int getCurrentConnections() {
		return currentConnections;
	}

	public void setCurrentConnections(int currentConnections) {
		this.currentConnections = currentConnections;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public ServiceChoiceStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(ServiceChoiceStrategy strategy) {
		this.strategy = strategy;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
