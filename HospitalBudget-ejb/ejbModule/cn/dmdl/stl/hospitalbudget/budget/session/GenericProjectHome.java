package cn.dmdl.stl.hospitalbudget.budget.session;

import cn.dmdl.stl.hospitalbudget.budget.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("genericProjectHome")
public class GenericProjectHome extends EntityHome<GenericProject> {

	public void setGenericProjectTheId(Integer id) {
		setId(id);
	}

	public Integer getGenericProjectTheId() {
		return (Integer) getId();
	}

	@Override
	protected GenericProject createInstance() {
		GenericProject genericProject = new GenericProject();
		return genericProject;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public GenericProject getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
