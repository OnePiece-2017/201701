package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.PermissionAssignmentExtend;
import cn.dmdl.stl.hospitalbudget.admin.entity.RoleInfo;
import cn.dmdl.stl.hospitalbudget.admin.entity.RolePermission;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.GlobalConstant;

@Name("grantPrivilegesHome")
public class GrantPrivilegesHome extends CriterionEntityHome<RoleInfo> {

	private static final long serialVersionUID = 1L;
	private String privileges;// 授予的权限
	private List<Object[]> permissionAssignmentColligateList;// 综合权限list

	@SuppressWarnings("unchecked")
	@Override
	public String update() {
		setMessage("");

		if (instance.getRoleInfoId() == GlobalConstant.ROLE_OF_ROOT) {
			setMessage(ConfigureCache.getMessageValue("reject_request"));
			log.warn("发现恶意行为：篡改系统账户");
			return null;
		}

		if (privileges != null && !"".equals(privileges)) {
			List<Object> permissionList = getEntityManager().createNativeQuery("select permission_info_id from permission_info where deleted = 0").getResultList();
			if (permissionList != null && permissionList.size() > 0) {
				Set<String> uniqueSet = new HashSet<String>();
				for (String permissionInfoId : privileges.split(",")) {// 允许8,,但是不允许,,8
					uniqueSet.add(permissionInfoId);
					try {
						if (!permissionList.contains(Integer.parseInt(permissionInfoId)) || !permissionInfoId.equals(String.valueOf(Integer.parseInt(permissionInfoId)))) {
							setMessage(ConfigureCache.getMessageValue("reject_request"));
							log.warn("发现恶意行为：页面权限不在系统范围内");
							return null;
						}
					} catch (NumberFormatException e) {
						setMessage(ConfigureCache.getMessageValue("reject_request"));
						log.warn("发现恶意行为：页面权限包含非法字符");
						return null;
					}
				}
				if (uniqueSet.size() != privileges.split(",").length) {
					setMessage(ConfigureCache.getMessageValue("reject_request"));
					log.warn("发现恶意行为：页面权限有重复值");
					return null;
				}
			} else {
				setMessage("错误：系统异常！");
				log.error("permission_info表无数据");
				return null;
			}
		}

		joinTransaction();
		String deleteSql = "delete from role_permission where role_info_id = " + instance.getRoleInfoId();
		getEntityManager().createNativeQuery(deleteSql).executeUpdate();

		if (privileges != null && !"".equals(privileges)) {
			for (String permissionInfoId : privileges.split(",")) {
				RolePermission rolePermission = new RolePermission();
				rolePermission.setRoleInfoId(instance.getRoleInfoId());
				rolePermission.setPermissionInfoId(Integer.parseInt(permissionInfoId));
				getEntityManager().persist(rolePermission);
			}
		}
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	public void setRoleInfoRoleInfoId(Integer id) {
		setId(id);
	}

	public Integer getRoleInfoRoleInfoId() {
		return (Integer) getId();
	}

	@Override
	protected RoleInfo createInstance() {
		RoleInfo roleInfo = new RoleInfo();
		return roleInfo;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	@SuppressWarnings("unchecked")
	public void wire() {
		getInstance();

		if (permissionAssignmentColligateList != null) {
			permissionAssignmentColligateList.clear();
		} else {
			permissionAssignmentColligateList = new ArrayList<Object[]>();
		}

		Map<Integer, Object[]> permissionInfoMap = new HashMap<Integer, Object[]>();
		List<Object[]> permissionInfoList = getEntityManager().createNativeQuery("select permission_info_id, permission_name, abbreviation, permission_key from permission_info where deleted = 0").getResultList();
		if (permissionInfoList != null && permissionInfoList.size() > 0) {
			for (Object[] permissionInfo : permissionInfoList) {
				permissionInfoMap.put(Integer.parseInt(permissionInfo[0].toString()), new Object[] { permissionInfo[2] });
			}
		}

		Map<Integer, List<Object[]>> permissionAssignmentMap = new HashMap<Integer, List<Object[]>>();
		List<PermissionAssignmentExtend> permissionAssignmentExtendList = getEntityManager().createQuery("select permissionAssignmentExtend from PermissionAssignmentExtend permissionAssignmentExtend").getResultList();
		if (permissionAssignmentExtendList != null && permissionAssignmentExtendList.size() > 0) {
			for (PermissionAssignmentExtend permissionAssignmentExtend : permissionAssignmentExtendList) {
				if (permissionAssignmentMap.containsKey(permissionAssignmentExtend.getPermissionAssignmentId())) {
					permissionAssignmentMap.get(permissionAssignmentExtend.getPermissionAssignmentId()).add(new Object[] { permissionAssignmentExtend.getPermissionInfoId(), permissionInfoMap.get(permissionAssignmentExtend.getPermissionInfoId())[0], permissionAssignmentExtend.getDisplayOrder() });
				} else {
					List<Object[]> list = new ArrayList<Object[]>();
					list.add(new Object[] { permissionAssignmentExtend.getPermissionInfoId(), permissionInfoMap.get(permissionAssignmentExtend.getPermissionInfoId())[0], permissionAssignmentExtend.getDisplayOrder() });
					permissionAssignmentMap.put(permissionAssignmentExtend.getPermissionAssignmentId(), list);
				}
			}
		}
		if (permissionAssignmentMap != null && permissionAssignmentMap.size() > 0) {
			for (Integer key : permissionAssignmentMap.keySet()) {
				Collections.sort(permissionAssignmentMap.get(key), new Comparator<Object[]>() {
					public int compare(Object[] arg0, Object[] arg1) {
						if (Integer.parseInt(arg0[2].toString()) < Integer.parseInt(arg1[2].toString())) {// 升序
							return -1;
						} else {
							return 1;
						}
					}
				});
			}
		}

		List<Object[]> permissionAssignmentList = getEntityManager().createNativeQuery("select permission_assignment_id, module_name, display_order from permission_assignment where deleted = 0").getResultList();
		if (permissionAssignmentList != null && permissionAssignmentList.size() > 0) {
			for (Object[] permissionAssignment : permissionAssignmentList) {
				List<Object[]> list = permissionAssignmentMap.get(Integer.parseInt(permissionAssignment[0].toString()));
				if (list != null && list.size() > 0) {
					permissionAssignmentColligateList.add(new Object[] { permissionAssignment[1], list, permissionAssignment[2] });
				}
			}
		}

		Collections.sort(permissionAssignmentColligateList, new Comparator<Object[]>() {
			public int compare(Object[] arg0, Object[] arg1) {
				if (Integer.parseInt(arg0[2].toString()) < Integer.parseInt(arg1[2].toString())) {// 升序
					return -1;
				} else {
					return 1;
				}
			}
		});

		List<Object[]> otherPermissionInfoList = getEntityManager().createNativeQuery("select permission_info_id, abbreviation from permission_info where deleted = 0 and permission_info_id not in (select permission_info_id from permission_assignment_extend) order by permission_info_id").getResultList();
		if (otherPermissionInfoList != null && otherPermissionInfoList.size() > 0) {
			List<Object[]> otherPermissionInfoColligateList = new ArrayList<Object[]>();
			for (Object[] otherPermissionInfo : otherPermissionInfoList) {
				otherPermissionInfoColligateList.add(new Object[] { otherPermissionInfo[0], otherPermissionInfo[1] });
			}
			permissionAssignmentColligateList.add(new Object[] { "其它", otherPermissionInfoColligateList, -1 });
		}

		if (null == privileges) {
			privileges = "";
			List<Object> rolePermissionIdList = getEntityManager().createNativeQuery("select permission_info_id from role_permission where role_info_id = " + instance.getRoleInfoId()).getResultList();
			if (rolePermissionIdList != null && rolePermissionIdList.size() > 0) {
				for (Object rolePermissionId : rolePermissionIdList) {
					privileges += rolePermissionId.toString() + ",";
				}
				if (!"".equals(privileges)) {
					privileges = privileges.substring(0, privileges.length() - 1);
				}
			}
		}
	}

	public boolean isWired() {
		return true;
	}

	public RoleInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public String getPrivileges() {
		return privileges;
	}

	public void setPrivileges(String privileges) {
		this.privileges = privileges;
	}

	public List<Object[]> getPermissionAssignmentColligateList() {
		return permissionAssignmentColligateList;
	}

}
