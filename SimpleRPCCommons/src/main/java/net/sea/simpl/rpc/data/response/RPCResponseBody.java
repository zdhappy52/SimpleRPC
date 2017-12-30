package net.sea.simpl.rpc.data.response;

import net.sea.simpl.rpc.data.RPCBody;

/**
 * RPC响应消息体
 * 
 * @author sea
 *
 */
public class RPCResponseBody extends RPCBody {
	private Object result;// 返回对象

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

}
