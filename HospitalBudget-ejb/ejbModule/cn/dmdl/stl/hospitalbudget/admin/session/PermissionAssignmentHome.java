package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.PermissionAssignment;
import cn.dmdl.stl.hospitalbudget.admin.entity.PermissionAssignmentExtend;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("permissionAssignmentHome")
public class PermissionAssignmentHome extends CriterionEntityHome<PermissionAssignment> {

	private static final long serialVersionUID = 1L;
	private JSONArray permissionPickListItems;
	private JSONArray permissionSortableItems;
	private Set<Integer> availablePermissionSet;
	private String permissionInfoIds;
	private String masterControl;

	@SuppressWarnings("unchecked")
	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select permission_assignment_id from permission_assignment where deleted = 0 and module_name = '" + instance.getModuleName() + "'") > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		if (permissionInfoIds != null && !"".equals(permissionInfoIds)) {
			Set<String> uniqueSet = new HashSet<String>();
			for (String permissionInfoId : permissionInfoIds.split(",")) {// 允许8,,但是不允许,,8
				uniqueSet.add(permissionInfoId);
				try {
					if (!availablePermissionSet.contains(Integer.parseInt(permissionInfoId)) || !permissionInfoId.equals(String.valueOf(Integer.parseInt(permissionInfoId)))) {
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
			if (uniqueSet.size() != permissionInfoIds.split(",").length) {
				setMessage(ConfigureCache.getMessageValue("reject_request"));
				log.warn("发现恶意行为：页面权限有重复值");
				return null;
			}
		}

		joinTransaction();
		int newDisplayOrder = 1;
		List<Object> newDisplayOrderList = getEntityManager().createNativeQuery("select ifnull(max(display_order), 0) + 1 as new_display_order from permission_assignment where deleted = 0").getResultList();
		if (newDisplayOrderList != null) {
			newDisplayOrder = Integer.parseInt(newDisplayOrderList.get(0).toString());
		}
		instance.setDisplayOrder(newDisplayOrder);
		getEntityManager().persist(instance);
		if (permissionInfoIds != null && !"".equals(permissionInfoIds)) {
			int displayOrder = 1;
			for (String permissionInfoId : permissionInfoIds.split(",")) {
				PermissionAssignmentExtend permissionAssignmentExtend = new PermissionAssignmentExtend();
				permissionAssignmentExtend.setPermissionAssignmentId(instance.getPermissionAssignmentId());
				permissionAssignmentExtend.setPermissionInfoId(Integer.parseInt(permissionInfoId));
				permissionAssignmentExtend.setDisplayOrder(displayOrder++);
				getEntityManager().persist(permissionAssignmentExtend);
			}
		}
		instance.setInsertTime(new Date());
		instance.setInsertUser(sessionToken.getUserInfoId());
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

		if (Assit.getResultSetSize("select permission_assignment_id from permission_assignment where deleted = 0 and module_name = '" + instance.getModuleName() + "' and permission_assignment_id != " + instance.getPermissionAssignmentId()) > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		if (permissionInfoIds != null && !"".equals(permissionInfoIds)) {
			Set<String> uniqueSet = new HashSet<String>();
			for (String permissionInfoId : permissionInfoIds.split(",")) {// 允许8,,但是不允许,,8
				uniqueSet.add(permissionInfoId);
				try {
					if (!availablePermissionSet.contains(Integer.parseInt(permissionInfoId)) || !permissionInfoId.equals(String.valueOf(Integer.parseInt(permissionInfoId)))) {
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
			if (uniqueSet.size() != permissionInfoIds.split(",").length) {
				setMessage(ConfigureCache.getMessageValue("reject_request"));
				log.warn("发现恶意行为：页面权限有重复值");
				return null;
			}
		}

		joinTransaction();
		getEntityManager().createNativeQuery("delete from permission_assignment_extend where permission_assignment_id = " + instance.getPermissionAssignmentId()).executeUpdate();

		if (permissionInfoIds != null && !"".equals(permissionInfoIds)) {
			int displayOrder = 1;
			for (String permissionInfoId : permissionInfoIds.split(",")) {
				PermissionAssignmentExtend permissionAssignmentExtend = new PermissionAssignmentExtend();
				permissionAssignmentExtend.setPermissionAssignmentId(instance.getPermissionAssignmentId());
				permissionAssignmentExtend.setPermissionInfoId(Integer.parseInt(permissionInfoId));
				permissionAssignmentExtend.setDisplayOrder(displayOrder++);
				getEntityManager().persist(permissionAssignmentExtend);
			}
		}
		instance.setUpdateTime(new Date());
		instance.setUpdateUser(sessionToken.getUserInfoId());
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getInstance();

		getEntityManager().createNativeQuery("delete from permission_assignment_extend where permission_assignment_id = " + instance.getPermissionAssignmentId()).executeUpdate();

		instance.setDeleted(true);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "removed";
	}

	public void setPermissionAssignmentPermissionAssignmentId(Integer id) {
		setId(id);
	}

	public Integer getPermissionAssignmentPermissionAssignmentId() {
		return (Integer) getId();
	}

	@Override
	protected PermissionAssignment createInstance() {
		PermissionAssignment permissionAssignment = new PermissionAssignment();
		return permissionAssignment;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	@SuppressWarnings("unchecked")
	public void wire() {
		getInstance();

		if (permissionPickListItems != null) {
			permissionPickListItems.clear();
		} else {
			permissionPickListItems = new JSONArray();
		}

		if (permissionSortableItems != null) {
			permissionSortableItems.clear();
		} else {
			permissionSortableItems = new JSONArray();
		}

		if (availablePermissionSet != null) {
			availablePermissionSet.clear();
		} else {
			availablePermissionSet = new HashSet<Integer>();
		}

		StringBuffer permissionSql = new StringBuffer();
		Set<Integer> selectedPermissionSet = new HashSet<Integer>();
		permissionSql.append("select permission_info_id, permission_name from permission_info where deleted = 0 and permission_info_id not in (select permission_info_id from permission_assignment_extend");
		if (isManaged()) {
			permissionSql.append(" where permission_assignment_id != " + instance.getPermissionAssignmentId());

			List<Object[]> selectedPermissionList = getEntityManager().createNativeQuery("select permission_assignment_extend.permission_info_id, permission_info.permission_name from permission_assignment_extend inner join permission_info on permission_info.permission_info_id = permission_assignment_extend.permission_info_id and permission_info.deleted = 0 where permission_assignment_extend.permission_assignment_id = " + instance.getPermissionAssignmentId() + " order by permission_assignment_extend.display_order").getResultList();
			if (selectedPermissionList != null && selectedPermissionList.size() > 0) {
				for (Object[] selectedPermission : selectedPermissionList) {
					selectedPermissionSet.add(Integer.parseInt(selectedPermission[0].toString()));
					JSONObject item = new JSONObject();
					item.accumulate("value", Integer.parseInt(selectedPermission[0].toString()));
					item.accumulate("label", selectedPermission[1].toString());
					permissionSortableItems.add(item);
				}
			}
		}
		permissionSql.append(")");
		List<Object[]> permissionList = getEntityManager().createNativeQuery(permissionSql.toString()).getResultList();
		if (permissionList != null && permissionList.size() > 0) {
			for (Object[] permission : permissionList) {
				availablePermissionSet.add(Integer.parseInt(permission[0].toString()));
				JSONObject item = new JSONObject();
				item.accumulate("value", Integer.parseInt(permission[0].toString()));
				item.accumulate("label", permission[1].toString());
				item.accumulate("selected", selectedPermissionSet.contains(Integer.parseInt(permission[0].toString())));
				permissionPickListItems.add(item);
			}
		}
	}

	public boolean isWired() {
		return true;
	}

	public PermissionAssignment getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public JSONArray getPermissionPickListItems() {
		return permissionPickListItems;
	}

	public JSONArray getPermissionSortableItems() {
		return permissionSortableItems;
	}

	public String getPermissionInfoIds() {
		return permissionInfoIds;
	}

	public void setPermissionInfoIds(String permissionInfoIds) {
		this.permissionInfoIds = permissionInfoIds;
	}

	public String getMasterControl() {
		return masterControl;
	}

	public void setMasterControl(String masterControl) {
		this.masterControl = masterControl;
	}

}
