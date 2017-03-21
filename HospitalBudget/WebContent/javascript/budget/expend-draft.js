jQuery(document).ready(function() {

	jQuery('.selectpicker').selectpicker();

	jQuery('#budgetYear').val(jQuery('#mainForm\\:budgetYearHidden').val());
	jQuery('#budgetYear').selectpicker('render');

	jQuery('#departmentInfoId').val(jQuery('#mainForm\\:departmentInfoIdHidden').val());
	jQuery('#departmentInfoId').selectpicker('render');

	jQuery('#fundsSourceId').val(jQuery('#mainForm\\:fundsSourceIdHidden').val());
	jQuery('#fundsSourceId').selectpicker('render');
});

function check_draft() {
	// TODO: 最后需要将这里的资金来源、主管科室验证删除，转而验证是否有待保存、待提交的内容
	var budgetYear = jQuery('#budgetYear').val();
	if (budgetYear == null || budgetYear == '') {
		___dynamic_function = function() {
			___selectpickerExpand('budgetYear');
		};
		___msg('温馨提示', '请选择预算年份！', {
			closed : ___dynamic_function
		});
		return false;
	}
	jQuery('#mainForm\\:budgetYearHidden').val(budgetYear);

	var fundsSourceId = jQuery('#fundsSourceId').val();
	if (fundsSourceId == null || fundsSourceId == '') {
		___dynamic_function = function() {
			___selectpickerExpand('fundsSourceId');
		};
		___msg('温馨提示', '请选择资金来源！', {
			closed : ___dynamic_function
		});
		return false;
	}
	jQuery('#mainForm\\:fundsSourceIdHidden').val(fundsSourceId);

	var departmentInfoId = jQuery('#departmentInfoId').val();
	if (departmentInfoId == null || departmentInfoId == '') {
		___dynamic_function = function() {
			___selectpickerExpand('departmentInfoId');
		};
		___msg('温馨提示', '请选择主管科室！', {
			closed : ___dynamic_function
		});
		return false;
	}
	jQuery('#mainForm\\:departmentInfoIdHidden').val(departmentInfoId);

	return true;
}

function check_submit() {
	// TODO: 最后需要将这里的资金来源、主管科室验证删除，转而验证是否有待保存、待提交的内容
	var budgetYear = jQuery('#budgetYear').val();
	if (budgetYear == null || budgetYear == '') {
		___dynamic_function = function() {
			___selectpickerExpand('budgetYear');
		};
		___msg('温馨提示', '请选择预算年份！', {
			closed : ___dynamic_function
		});
		return false;
	}
	jQuery('#mainForm\\:budgetYearHidden').val(budgetYear);

	var fundsSourceId = jQuery('#fundsSourceId').val();
	if (fundsSourceId == null || fundsSourceId == '') {
		___dynamic_function = function() {
			___selectpickerExpand('fundsSourceId');
		};
		___msg('温馨提示', '请选择资金来源！', {
			closed : ___dynamic_function
		});
		return false;
	}
	jQuery('#mainForm\\:fundsSourceIdHidden').val(fundsSourceId);

	var departmentInfoId = jQuery('#departmentInfoId').val();
	if (departmentInfoId == null || departmentInfoId == '') {
		___dynamic_function = function() {
			___selectpickerExpand('departmentInfoId');
		};
		___msg('温馨提示', '请选择主管科室！', {
			closed : ___dynamic_function
		});
		return false;
	}
	jQuery('#mainForm\\:departmentInfoIdHidden').val(departmentInfoId);

	return true;
}
