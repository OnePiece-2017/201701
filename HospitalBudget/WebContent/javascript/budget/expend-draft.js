jQuery(document).ready(function() {

	jQuery('.selectpicker').selectpicker();

	jQuery('#budgetYear').val(jQuery('#mainForm\\:budgetYearHidden').val());
	jQuery('#budgetYear').selectpicker('render');

	jQuery('#departmentInfoId').val(jQuery('#mainForm\\:departmentInfoIdHidden').val());
	jQuery('#departmentInfoId').selectpicker('render');

	jQuery('#fundsSourceId').val(jQuery('#mainForm\\:fundsSourceIdHidden').val());
	jQuery('#fundsSourceId').selectpicker('render');

	jQuery('#budgetYear, #fundsSourceId, #departmentInfoId').change(triggerRenewDataContainer);

	jQuery('.draft-table-head .btn-reminder-service').click(function() {
		showLayer();
		if (jQuery(this).hasClass('running')) {
			jQuery(this).removeClass('running');
			jQuery(this).addClass('stopped');
			___sgInputboxStopReminderService();
		} else {
			jQuery(this).removeClass('stopped');
			jQuery(this).addClass('running');
			___sgInputboxStartReminderService();
		}
		setTimeout(function() {
			hideLayer();
		}, 128);// 防止恶意点击
	});

	jQuery('.draft-table-title, .draft-table-body').niceScroll({
	    cursorcolor : '#4d0acf',
	    cursorwidth : 8,
	    cursoropacitymax : 0.8,
	    touchbehavior : false,
	    autohidemode : true
	});

	refreshVisualPannel();// 立即刷新
	triggerRenewDataContainer();// 触发更新数据容器函数
});

function refreshVisualPannel() {
	if (jQuery('.draft-table-body .data-container .item-outer').length > 0) {
		jQuery('.draft-table-body .no-data').hide();
		jQuery('.draft-table-body .data-container').show();
	} else {
		jQuery('.draft-table-body .data-container').hide();
		jQuery('.draft-table-body .no-data').show();
	}
}

function triggerRenewDataContainer() {
	var budgetYear = jQuery('#budgetYear').val();
	var fundsSourceId = jQuery('#fundsSourceId').val();
	var departmentInfoId = jQuery('#departmentInfoId').val();
	var args = {
	    budgetYear : budgetYear,
	    fundsSourceId : fundsSourceId,
	    departmentInfoId : departmentInfoId
	};
	showLayer();
	jQuery('.draft-table-body .data-container').empty();// 1、清空数据容器
	jQuery('.draft-table-body').getNiceScroll().resize();// 2、重绘滚动条
	callRenewDataContainer(JSON.stringify(args));// 3、调用更新数据容器AJAX
}

function completedRenewDataContainer(data) {
	parseProject(data['generic'], 'generic', '项目');// 4、解析项目并放入数据容器
	parseProject(data['routine'], 'routine', '常规');// 5、解析常规并放入数据容器
	jQuery('.draft-table-body').getNiceScroll().resize();// 6、重绘滚动条
	setTimeout(function() {
		hideLayer();
		refreshVisualPannel();// 7、刷新可视化面板
	}, 128);// 防止恶意点击
}

function parseProject(projectArray, namespace, projectNature) {
	if (projectArray != null && projectArray.length > 0) {
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
		processLeaf(projectArray);
		if (html != '') {
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
			jQuery('.draft-table-body .data-container [namespace="' + namespace + '"] .field-function .opr-locking:not(.not-enabled)').click(function() {
				showLayer();
				if (!jQuery(this).hasClass('not-enabled')) {
					if (jQuery(this).hasClass('activated')) {
						jQuery(this).removeClass('activated');
						jQuery(this).parents('.item-outer').removeClass('locking');
					} else {
						jQuery(this).addClass('activated');
						jQuery(this).parents('.item-outer').addClass('locking');
					}
				}
				setTimeout(function() {
					hideLayer();
				}, 128);// 防止恶意点击
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
	}
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
