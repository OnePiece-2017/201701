package cn.dmdl.stl.hospitalbudget.execute.session;


import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

import cn.dmdl.stl.hospitalbudget.execute.entity.EarnConfirmProduct;

@Name("earnConfirmProductHome")
public class EarnConfirmProductHome extends EntityHome<EarnConfirmProduct> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 678133151952624931L;

	public void setEarnConfirmProductEarnConfirmProductId(Integer id) {
		setId(id);
	}

	public Integer getEarnConfirmProductEarnConfirmProductId() {
		return (Integer) getId();
	}

	@Override
	protected EarnConfirmProduct createInstance() {
		EarnConfirmProduct earnConfirmProduct = new EarnConfirmProduct();
		return earnConfirmProduct;
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

	public EarnConfirmProduct getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
