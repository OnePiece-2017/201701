package cn.dmdl.stl.hospitalbudget.admin.entity;

// Generated 2016-10-18 22:51:16 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * PermissionAssignmentExtend generated by hbm2java
 */
@Entity
@Table(name = "permission_assignment_extend", catalog = "hospital_budget")
public class PermissionAssignmentExtend implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer permissionAssignmentExtendId;
	private int permissionAssignmentId;
	private int permissionInfoId;
	private int displayOrder;
	private boolean masterControl;

	public PermissionAssignmentExtend() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "permission_assignment_extend_id", unique = true, nullable = false)
	public Integer getPermissionAssignmentExtendId() {
		return this.permissionAssignmentExtendId;
	}

	public void setPermissionAssignmentExtendId(Integer permissionAssignmentExtendId) {
		this.permissionAssignmentExtendId = permissionAssignmentExtendId;
	}

	@Column(name = "permission_assignment_id", nullable = false)
	public int getPermissionAssignmentId() {
		return this.permissionAssignmentId;
	}

	public void setPermissionAssignmentId(int permissionAssignmentId) {
		this.permissionAssignmentId = permissionAssignmentId;
	}

	@Column(name = "permission_info_id", nullable = false)
	public int getPermissionInfoId() {
		return this.permissionInfoId;
	}

	public void setPermissionInfoId(int permissionInfoId) {
		this.permissionInfoId = permissionInfoId;
	}

	@Column(name = "display_order", nullable = false)
	public int getDisplayOrder() {
		return this.displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Column(name = "master_control", nullable = false)
	public boolean isMasterControl() {
		return this.masterControl;
	}

	public void setMasterControl(boolean masterControl) {
		this.masterControl = masterControl;
	}

}
