package cn.dmdl.stl.hospitalbudget.common.entity;

import java.util.HashMap;
import java.util.Map;

public class FakeEntity {

	private Map<String, Object> properties;

	public void assign(String key, Object value) {
		if (null == properties) {
			properties = new HashMap<String, Object>();
		}
		properties.put(key, value);
	}

	public Object value(String key) {
		return properties != null ? properties.get(key) : null;
	}

}
