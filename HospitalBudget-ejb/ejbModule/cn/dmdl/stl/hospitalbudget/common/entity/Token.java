package cn.dmdl.stl.hospitalbudget.common.entity;

/**
 * 令牌--别删除   或许可以有别的用途  保留package
 */
public class Token {

	private String key;// TODO: 保留servlet 改功能为用户类型检测 免费用户，付费用户、、、、、签约客户 检测使用期限
	private long time;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

}
