var ___cache = {};

jQuery(document).ready(function() {

	jQuery('.selectpicker').selectpicker();

	jQuery('#budgetYear').val(jQuery('#budgetYearHidden').val());
	jQuery('#budgetYear').selectpicker('render');

	jQuery('#departmentInfoId').val(jQuery('#departmentInfoIdHidden').val());
	jQuery('#departmentInfoId').selectpicker('render');

	jQuery('#fundsSourceId').val(jQuery('#fundsSourceIdHidden').val());
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
	___sgInputboxStopReminderService();// 默认停止服务

	___cache['total-amount-value'] = jQuery('.draft-table-head .total-amount-value span');// 缓存节点（总金额）

	jQuery('.draft-table-title, .draft-table-body').niceScroll({
	    cursorcolor : '#4d0acf',
	    cursorwidth : 8,
	    cursoropacitymax : 0.8,
	    touchbehavior : false,
	    autohidemode : true
	});

	refreshVisualPannel();// 立即刷新
	triggerRenewDataContainer();// 触发更新数据容器函数
	computeTotalAmount();// 实时计算总金额

	var forTest = !false; // TODO: 仅供测试
	if (forTest) {
		setTimeout(function() {
			jQuery('#fundsSourceId').val('1');
			jQuery('#fundsSourceId').selectpicker('render');
			jQuery('#departmentInfoId').val('2');
			jQuery('#departmentInfoId').selectpicker('render');
			triggerRenewDataContainer();
		}, 256);
	}
});

function refreshVisualPannel() {
	if (jQuery('.draft-table-body .data-container .item-outer').length > 0) {
		jQuery('.not-exist-data-panel').hide();
		jQuery('.exist-data-panel').show();
	} else {
		jQuery('.exist-data-panel').hide();
		jQuery('.not-exist-data-panel').show();
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
	parseProject(data['new_generic'], 'generic', '项目');// 4、解析项目并放入数据容器
	parseProject(data['new_routine'], 'routine', '常规');// 5、解析常规并放入数据容器
	bindingCtrlEvents();// 6、绑定事件
	recoverProject(data['old_generic'], 'generic');// 7、恢复项目并修改数据容器
	recoverProject(data['old_routine'], 'routine');// 8、恢复项目并修改数据容器
	jQuery('.draft-table-body').getNiceScroll().resize();// 9、重绘滚动条
	setTimeout(function() {
		hideLayer();
		refreshVisualPannel();// a、刷新可视化面板
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
				html += '	<div class="item-outer' + (hasSub ? ' read-only' : ' write-able') + '" namespace="' + namespace + '" project-id="' + node['id'] + '" project-nature="' + projectNature + '">';
				html += '		<div class="item-inner">';
				html += '			<div class="toggle' + (hasSub ? ' expand' : '') + '">';
				html += '				<span></span>';
				html += '			</div>';
				html += '			<div class="generic-field edge-end field-id">';
				html += '				<span>' + node['id'] + '</span>';
				html += '			</div>';
				html += '			<div class="generic-field edge-end field-project-name">';
				html += '				<span>' + node['projectName'] + '</span>';
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
				html += '			<div class="generic-field edge-end field-project-source">';
				html += '				<span>' + (hasSub ? '' : '<textarea id="' + (namespace + '_projectSource_' + node['id']) + '" class="form-control"></textarea>') + '</span>';
				html += '			</div>';
				html += '			<div class="generic-field edge-end field-project-amount">';
				html += '				<span>' + (hasSub ? '' : '<input id="' + (namespace + '_projectAmount_' + node['id']) + '" class="form-control"></input>') + '</span>';
				html += '			</div>';
				html += '			<div class="generic-field edge-end field-formula-remark">';
				html += '				<span>' + (hasSub ? '' : '<textarea id="' + (namespace + '_formulaRemark_' + node['id']) + '" class="form-control"></textarea>') + '</span>';
				html += '			</div>';
				html += '			<div class="generic-field field-function">';
				html += '				<span id="' + (namespace + '_attachment_' + node['id']) + '" class="operation-item opr-attachment' + (hasSub ? ' not-enabled' : '') + '"></span>';
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
				if (jQuery(this).hasClass('activated')) {
					jQuery(this).removeClass('activated');
					jQuery(this).parents('.item-outer').removeClass('locking');
				} else {
					jQuery(this).addClass('activated');
					jQuery(this).parents('.item-outer').addClass('locking');
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
			// 绑定附件插件
			jQuery('.draft-table-body .data-container [namespace="' + namespace + '"] .field-function .opr-attachment:not(.not-enabled)').click(function() {
				showLayer();
				var tempHandler = jQuery(this);// 临时句柄
				if (jQuery(this).hasClass('activated')) {
					setTimeout(function() {
						hideLayer();
					}, 512);// 防止恶意点击
				} else {
					sgFileupload['install']({
					    'target' : this.id,
					    'alias' : '',
					    'source' : 'jjjjjjjjjjjjjjjjjjjjjjjjjj',
					    'class' : 'sg-fu-custom--attachment',
					    'completed' : function() {
						    tempHandler.addClass('activated');
						    tempHandler.click();
					    }
					});
				}
			});
		}
	}
}

function bindingCtrlEvents() {
	jQuery('.draft-table-body .data-container .item-outer .field-project-amount input').blur(function() {
		if (jQuery.isNumeric(this.value)) {
			this.value = Number(this.value).toFixed(2);
		} else {
			this.value = '';
		}
	});
}

function recoverProject(projectArray, namespace) {
	if (projectArray != null && projectArray.length > 0) {
		for (var i = 0, len = projectArray.length; i < len; i++) {
			var itemOuter = jQuery('.draft-table-body .data-container .item-outer[namespace="' + namespace + '"][project-id="' + projectArray[i]['projectId'] + '"]');
			if (itemOuter.length > 0) {
				var projectAmount = (projectArray[i]['projectAmount'] * 1E-4).toFixed(2);
				if (itemOuter.hasClass('read-only')) {
					itemOuter.find('.field-project-amount span').html(projectAmount);
				} else {
					itemOuter.find('.field-project-amount input').val(projectAmount);
				}
				var projectSource = (projectArray[i]['projectSource']);
				if (itemOuter.hasClass('read-only')) {
					itemOuter.find('.field-project-source span').html(projectSource);
				} else {
					itemOuter.find('.field-project-source textarea').val(projectSource);
				}
				var formulaRemark = (projectArray[i]['formulaRemark']);
				if (itemOuter.hasClass('read-only')) {
					itemOuter.find('.field-formula-remark span').html(formulaRemark);
				} else {
					itemOuter.find('.field-formula-remark textarea').val(formulaRemark);
				}
			}
		}
	}
}

function computeTotalAmount() {
	var result = 0;
	jQuery('.draft-table-body .data-container .write-able:not(.locking)').each(function() {
		var temp = jQuery(this).find('.field-project-amount input').val();
		if (jQuery.isNumeric(temp)) {
			temp = Number(temp);
		} else {
			temp = 0;
		}
		result += temp;
	});
	___cache['total-amount-value'].html(result.toFixed(2));
	setTimeout(computeTotalAmount, 256);// 间隔一个周期再次进行计算
}

function triggerSaveDataContainer() {
	showLayer();
	var allowExec = false;
	var args = {
	    'generic' : [],
	    'routine' : []
	};
	// 数据采集（项目、常规）
	var callback = null;// 提示框回调函数
	jQuery('.draft-table-body .data-container .item-outer.write-able:not(.locking)').each(function() {
		var tempHandler = null;// 临时句柄

		// 预算金额
		var projectAmount = jQuery(this).find('.field-project-amount input').val();
		tempHandler = jQuery(this).find('.field-project-amount input');
		if (!jQuery.isNumeric(projectAmount)) {
			callback = function() {
				tempHandler.focus();
			};
			allowExec = false;// 不允许执行
			return false;// 结束遍历
		}

		// 项目来源
		var projectSource = jQuery(this).find('.field-project-source textarea').val();

		// 金额计算依据及备注
		var formulaRemark = jQuery(this).find('.field-formula-remark textarea').val();

		// 附件
		var attachment = sgFileupload['getSource'](jQuery(this).find('.field-function .opr-attachment').attr('id'));

		args[jQuery(this).attr('namespace')].push({
		    'budgetYear' : jQuery('#budgetYear').val(),
		    'projectId' : jQuery(this).attr('project-id'),
		    'projectAmount' : Number(projectAmount).toFixed(2) * 1E4,
		    'projectSource' : projectSource,
		    'formulaRemark' : formulaRemark,
		    'attachment' : attachment
		});
		allowExec = true;// 允许执行
	});

	if (allowExec) {
		callSaveDataContainer(JSON.stringify(args));// 调用保存数据容器AJAX
	} else {
		hideLayer();
		if (callback != null) {
			___msg('温馨提示', '数据不完整，不能执行保存操作！', {
				closed : callback
			});
		} else {
			___msg('温馨提示', '无可保存的条目！');
		}
	}
}

function completedSaveDataContainer(data) {
	hideLayer();
	if (data != null && 'INVOKE_SUCCESS' == data.invoke_result) {
		___msg('温馨提示', data.result_message);
	} else {
		___msg('错误提示', data.result_message);
	}
}

function check_submit() {
	return true;
}
