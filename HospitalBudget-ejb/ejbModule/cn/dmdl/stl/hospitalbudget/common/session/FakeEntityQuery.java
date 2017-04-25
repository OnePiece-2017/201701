package cn.dmdl.stl.hospitalbudget.common.session;

import java.util.ArrayList;
import java.util.List;

import cn.dmdl.stl.hospitalbudget.common.entity.FakeEntity;

/**
 * 伪实体查询
 * 
 * <pre>
 * 注意：如果报类似这样的错误：
 * could not read column value from result set: the_id; Column 'the_id' not found.
 * 尝试将sql包裹一下
 * select * from (your sql) as recordset
 * </pre>
 * 
 */
public abstract class FakeEntityQuery extends CriterionNativeQuery<FakeEntity> {

	private static final long serialVersionUID = 1L;
	private String[] fakeEntityColumns;

	@Override
	public List<FakeEntity> getResultList() {
		List<FakeEntity> resultList = new ArrayList<FakeEntity>();
		List<?> originalList = super.getResultList();
		if (fakeEntityColumns != null && fakeEntityColumns.length > 0) {
			if (originalList != null) {
				for (Object row : originalList) {
					FakeEntity fakeEntity = new FakeEntity();
					if (row instanceof Object[]) {
						Object[] columns = (Object[]) row;
						for (int i = 0, j = columns.length, k = fakeEntityColumns.length; i < j && i < k; i++) {
							fakeEntity.assign(fakeEntityColumns[i], columns[i]);
						}
					} else {
						fakeEntity.assign(fakeEntityColumns[0], row);
					}
					resultList.add(fakeEntity);
				}
			}
		}
		return resultList;
	}

	public String[] getFakeEntityColumns() {
		return fakeEntityColumns;
	}

	public void setFakeEntityColumns(String... fakeEntityColumns) {
		this.fakeEntityColumns = fakeEntityColumns;
	}

}
