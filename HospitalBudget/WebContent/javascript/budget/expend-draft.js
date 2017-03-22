jQuery(document).ready(function() {
	showLayer();

	jQuery('.selectpicker').selectpicker();

	jQuery('#budgetYear').val(jQuery('#mainForm\\:budgetYearHidden').val());
	jQuery('#budgetYear').selectpicker('render');

	jQuery('#departmentInfoId').val(jQuery('#mainForm\\:departmentInfoIdHidden').val());
	jQuery('#departmentInfoId').selectpicker('render');

	jQuery('#fundsSourceId').val(jQuery('#mainForm\\:fundsSourceIdHidden').val());
	jQuery('#fundsSourceId').selectpicker('render');

	jQuery('.draft-table-head .btn-reminder-service').click(function() {
		if (jQuery(this).hasClass('running')) {
			jQuery(this).removeClass('running');
			jQuery(this).addClass('stopped');
			___sgInputboxStopReminderService();
		} else {
			jQuery(this).removeClass('stopped');
			jQuery(this).addClass('running');
			___sgInputboxStartReminderService();
		}
	});

	jQuery('.draft-table-title, .draft-table-body').niceScroll({
	    cursorcolor : '#4d0acf',
	    cursorwidth : 8,
	    cursoropacitymax : 0.8,
	    touchbehavior : false,
	    autohidemode : true
	});

	// 模拟数据
	var genericProject = [ {
	    id : 19,
	    projectName : '办公用品购置',
	    fundsSource : '自有资金',
	    departmentName : '内科',
	    subset : []
	}, {
	    id : 1,
	    projectName : '3D打印技术研究',
	    fundsSource : '自有资金',
	    departmentName : '内科',
	    subset : [ {
	        id : 11,
	        projectName : '肾脏打印',
	        fundsSource : '自有资金',
	        departmentName : '内科',
	        subset : [ {
	            id : 12,
	            projectName : '肾脏细胞样本分析',
	            fundsSource : '自有资金',
	            departmentName : '内科',
	            subset : []
	        }, {
	            id : 13,
	            projectName : '肾脏打印原材料采购',
	            fundsSource : '自有资金',
	            departmentName : '内科',
	            subset : [ {
	                id : 14,
	                projectName : '肾脏打印原材料采购（进口）',
	                fundsSource : '自有资金',
	                departmentName : '内科',
	                subset : []
	            }, {
	                id : 15,
	                projectName : '肾脏打印原材料采购（国产）',
	                fundsSource : '自有资金',
	                departmentName : '内科',
	                subset : []
	            } ]
	        } ]
	    }, {
	        id : 16,
	        projectName : '骨关节打印',
	        fundsSource : '自有资金',
	        departmentName : '内科',
	        subset : [ {
	            id : 17,
	            projectName : '牙齿打印',
	            fundsSource : '自有资金',
	            departmentName : '内科',
	            subset : []
	        }, {
	            id : 18,
	            projectName : '颅骨打印',
	            fundsSource : '自有资金',
	            departmentName : '内科',
	            subset : []
	        } ]
	    } ]
	} ];
	var routineProject = [ {
	    id : 19,
	    projectName : '健身卡充值',
	    fundsSource : '自有资金',
	    departmentName : '外科',
	    subset : []
	} ];
	jQuery('.draft-table-body .data-container').empty();// 1、清空数据容器
	jQuery('.draft-table-body').getNiceScroll().resize();// 2、重绘滚动条
	parseProject(genericProject, 'generic', '项目');// 3、解析项目并放入数据容器
	parseProject(routineProject, 'routine', '常规');// 4、解析常规并放入数据容器
	jQuery('.draft-table-body').getNiceScroll().resize();// 5、重绘滚动条
	hideLayer();
});

function parseProject(projectArray, namespace, projectNature) {
	var html = '';
	var processLeaf = function(leaf) {
		for (var i = 0; i < leaf.length; i++) {
			var node = leaf[i];
			var hasSub = node['subset'].length > 0;
			html += '<li>';
			html += '	<div class="item-outer" namespace="' + namespace + '" project-nature="' + projectNature + '">';
			html += '		<div class="item-inner">';
			html += '			<div class="toggle' + (hasSub ? ' expand' : '') + '">';
			html += '				<span></span>';
			html += '			</div>';
			html += '			<div class="generic-field edge-end field-id">';
			html += '				<span>' + node['id'] + '</span>';
			html += '			</div>';
			html += '			<div class="generic-field edge-end field-project-nature">';
			html += '				<span>' + projectNature + '</span>';
			html += '			</div>';
			html += '			<div class="generic-field edge-end field-funds-source">';
			html += '				<span>' + node['fundsSource'] + '</span>';
			html += '			</div>';
			html += '			<div class="generic-field edge-end field-department-name">';
			html += '				<span>' + node['departmentName'] + '</span>';
			html += '			</div>';
			html += '			<div class="generic-field edge-end field-project-name">';
			html += '				<span>' + node['projectName'] + '</span>';
			html += '			</div>';
			html += '			<div class="generic-field edge-end field-project-source">';
			html += '				<span>' + (hasSub ? '#禁止编辑' : '<textarea id="' + (namespace + '_projectSource_' + node['id']) + '" class="form-control"></textarea>') + '</span>';
			html += '			</div>';
			html += '			<div class="generic-field edge-end field-project-amount">';
			html += '				<span>' + (hasSub ? '' : '<input id="' + (namespace + '_projectAmount_' + node['id']) + '" class="form-control"></input>') + '</span>';
			html += '			</div>';
			html += '			<div class="generic-field edge-end field-formula-remark">';
			html += '				<span>' + (hasSub ? '#禁止编辑' : '<textarea id="' + (namespace + '_formulaRemark_' + node['id']) + '" class="form-control"></textarea>') + '</span>';
			html += '			</div>';
			html += '			<div class="generic-field field-function">';
			html += '				<span class="operation-item opr-attachment activated' + (hasSub ? ' not-enabled' : '') + '"></span>';
			html += '				<span class="operation-item opr-locking' + (hasSub ? ' not-enabled' : '') + '"></span>';
			html += '				<span class="operation-item opr-reference' + (hasSub ? ' not-enabled' : '') + '"></span>';
			html += '			</div>';
			html += '		</div>';
			html += '	</div>';
			if (hasSub) {
				html += '<ul>';
				processLeaf(node['subset']);
				html += '</ul>';
			}
			html += '</li>';
		}
	};
	if (projectArray != null) {
		processLeaf(projectArray);
	}
	jQuery('.draft-table-body .data-container').append(html);
	jQuery('.draft-table-body .data-container [namespace="' + namespace + '"] textarea[id^="' + namespace + '_projectSource_"]').each(function() {
		___sgInputbox({
		    id : this.id,
		    alias : '项目来源' + ___surroundContents(jQuery(this).parents('.item-outer').attr('project-nature'), 'ID: ' + this.id.replace(namespace + '_projectSource_', '')),
		    threshold : 45,
		    notify : true
		});
		___textRestrictById(this.id, 45);
	});
	jQuery('.draft-table-body .data-container [namespace="' + namespace + '"] textarea[id^="' + namespace + '_formulaRemark_"]').each(function() {
		___sgInputbox({
		    id : this.id,
		    alias : '金额计算依据及备注' + ___surroundContents(jQuery(this).parents('.item-outer').attr('project-nature'), 'ID: ' + this.id.replace(namespace + '_formulaRemark_', '')),
		    threshold : 300,
		    notify : true
		});
		___textRestrictById(this.id, 300);
	});
	jQuery('.draft-table-body .data-container [namespace="' + namespace + '"] .toggle').click(function() {
		showLayer();
		var cache_ul = jQuery(this).parents('.item-outer').next('ul');
		if (cache_ul.length > 0) {
			jQuery(this).toggleClass(function() {
				if (jQuery(this).hasClass('collapse')) {
					jQuery(this).removeClass('collapse');
					return 'expand';
				} else {
					jQuery(this).removeClass('expand');
					return 'collapse';
				}
			});
			cache_ul.slideToggle(128, function() {
				jQuery('.draft-table-body').getNiceScroll().resize();
				hideLayer();
			});
		} else {
			hideLayer();
		}
	});
}

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
