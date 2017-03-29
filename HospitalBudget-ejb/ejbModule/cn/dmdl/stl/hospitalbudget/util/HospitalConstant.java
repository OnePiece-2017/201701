package cn.dmdl.stl.hospitalbudget.util;

/**
 * 预算项目常量类
 * @author HASEE
 *
 */
public class HospitalConstant {
	//编制表状态
	public final static int DRAFTSTATUS_SAVE = 0;//保存
	public final static int DRAFTSTATUS_AUDIT = 1;//审核中
	public final static int DRAFTSTATUS_RETURN = 2;//被退回
	public final static int DRAFTSTATUS_FINISH = 3;//已完成
	public final static int DRAFTSTATUS_TAKEBACK = 4;//追回
	
	//汇总表状态
	public final static int COLLECTIONSTATUS_WAIT = 0;//待处理
	public final static int COLLECTIONSTATUS_FINISH = 1;//已下达
	public final static int COLLECTIONSTATUS_RETURN = 2;//被追回
	
	//汇总表类型
	public final static int COLLECTIONTYPE_INCOME = 1;//收入
	public final static int COLLECTIONTYPE_EXPEND = 2;//支出
	
}
