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
	public final static int COLLECTIONSTATUS_TAKEBACK = 2;//被追回
	
	//汇总表类型
	public final static int COLLECTIONTYPE_INCOME = 1;//收入
	public final static int COLLECTIONTYPE_EXPEND = 2;//支出
	
	//编制类型
	public final static int DRAFT_TYPE_DRAFT = 0;//编制
	public final static int DRAFT_TYPE_ADJUSTMENT = 1;//调整
	
	//流程类型
	public final static int PROCESS_TYPE_NORMAL = 1;//常规流程
	public final static int PROCESS_TYPE_PROJECT = 2;//项目流程
	
	public final static int PROCESS_INCOME_DRAFT = 1;//收入编制
	public final static int PROCESS_EXPEND_DRAFT = 2;//支出编制
	public final static int PROCESS_INCOME_EXECUTE = 3;//收入执行
	public final static int PROCESS_EXPEND_EXECUTE = 4;//支出执行
	
	//项目类型
	public final static int PROJECT_IS_AUDIT = 1;//是审计项目
	
}
