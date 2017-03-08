package cn.dmdl.stl.hospitalbudget.budget.session;

import cn.dmdl.stl.hospitalbudget.budget.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("genericProjectList")
public class GenericProjectList extends EntityQuery<GenericProject> {

	private static final String EJBQL = "select genericProject from GenericProject genericProject";

	private static final String[] RESTRICTIONS = { "lower(genericProject.theValue) like lower(concat(#{genericProjectList.genericProject.theValue},'%'))", "lower(genericProject.theDescription) like lower(concat(#{genericProjectList.genericProject.theDescription},'%'))", };

	private GenericProject genericProject = new GenericProject();

	public GenericProjectList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public GenericProject getGenericProject() {
		return genericProject;
	}
}
