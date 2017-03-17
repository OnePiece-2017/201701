var menuInfoJsonArray = null;
var quickStartConfig = null;
var intervalID___highlight = null;

jQuery(document).ready(function() {

	jQuery('.ctrl-left .inner-area').niceScroll({
	    cursorcolor : '#4d0acf',
	    cursorwidth : 8,
	    cursoropacitymax : 0.8,
	    touchbehavior : false,
	    autohidemode : true
	});

	jQuery('.ctrl-right .inner-area').niceScroll({
	    cursorcolor : '#4d0acf',
	    cursorwidth : 8,
	    cursoropacitymax : 0.8,
	    touchbehavior : false,
	    autohidemode : true
	});

	var funcCode = '';// 菜单HTML代码
	var disposeLeaf = function(leaf) {
		for (var i = 0; i < leaf.length; i++) {
			var node = leaf[i];
			funcCode += '<li><div class="func-outer' + (0 == node['leaf'].length && node['tabUrl'] != null && node['tabUrl'] != '' ? ' allow-select' : '') + '"><div class="func-inner">';
			funcCode += '<span class="func-logo"' + (node['iconSrc'] != null && node['iconSrc'] != '' ? ' style="background: RGBA(0, 0, 0, 0) url(' + node['iconSrc'] + ') no-repeat center;"' : '') + '></span>';
			funcCode += '<span class="func-name"';
			if (0 == node['leaf'].length) {
				funcCode += ' tab-index="' + node['theId'] + '"';
			}
			funcCode += '>' + node['theValue'] + '</span>';
			if (node['leaf'].length > 0) {
				funcCode += '<span class="toggle collapse"></span></div></div><ul style="display: none;">';
				disposeLeaf(node['leaf']);
				funcCode += '</ul>';
			} else if (node['tabUrl'] != null && node['tabUrl'] != '') {
				funcCode += '</div></div>';
			} else {
				funcCode += '<span class="func-unopened" title="该功能处于停用状态！"></span></div></div>';
			}
			funcCode += '</li>';
		}
	};
	if (menuInfoJsonArray != null) {
		disposeLeaf(menuInfoJsonArray);
	}
	jQuery('.ctrl-left .function-container').html(funcCode);
	jQuery('.ctrl-left .func-outer').click(function() {
		showLayer();
		var cache_ul = jQuery(this).next('ul');
		if (cache_ul.length > 0) {
			jQuery(this).find('.toggle').toggleClass(function() {
				if (jQuery(this).hasClass('collapse')) {
					jQuery(this).removeClass('collapse');
					return 'expand';
				} else {
					jQuery(this).removeClass('expand');
					return 'collapse';
				}
			});
			cache_ul.slideToggle(128, function() {
				jQuery('.ctrl-left .inner-area').getNiceScroll().resize();
				hideLayer();
			});
		} else {
			var delay = 0;
			if (jQuery(this).hasClass('allow-select')) {
				var funcName = jQuery(this).find('.func-name');
				var tabIndex = funcName.attr('tab-index');
				var tabName = funcName.html();
				if (jQuery(this).hasClass('selected')) {
					jQuery(this).removeClass('selected');
					jQuery('.ctrl-right .item-block[tab-index="' + tabIndex + '"]').remove();
				} else {
					jQuery(this).addClass('selected');
					var itemBlock = '';
					itemBlock += '<div class="item-block" tab-index="' + tabIndex + '"><div class="block-content">';
					itemBlock += '<span class="btn-delete" onclick="deselect(' + tabIndex + ');"></span><span class="text" onclick="linkageCtrlLeft(' + tabIndex + ');">' + tabName + '</span></div></div>';
					jQuery('.ctrl-right .inner-area').append(itemBlock);
				}
				jQuery('.ctrl-right .inner-area').getNiceScroll().resize();
				jQuery('.ctrl-right .inner-area').getNiceScroll(0).doScrollTop(jQuery('.ctrl-right .inner-area')[0].scrollHeight, 0);
				delay = 128;
			}
			setTimeout(function() {
				hideLayer();
			}, delay);// 防止恶意点击
		}
	});

	loadSelected();// 加载已选择的功能菜单

});

function linkageCtrlLeft(tabIndex) {
	showLayer();
	var activated = jQuery('.ctrl-left .func-name[tab-index="' + tabIndex + '"]').parents('.func-inner');
	var funcOuterRefer = activated.parents('ul').prev('.func-outer');
	if (funcOuterRefer != null && funcOuterRefer.length > 0) {
		for (var i = funcOuterRefer.length - 1; i >= 0; --i) {
			if (jQuery(funcOuterRefer[i]).next('ul').is(':hidden')) {
				jQuery(funcOuterRefer[i]).next('ul').show();
				jQuery(funcOuterRefer[i]).find('.toggle').removeClass('collapse').addClass('expand');
			}
		}
	}
	jQuery('.ctrl-left .inner-area').getNiceScroll().resize();
	var offsetContainer = jQuery('.ctrl-left .func-outer:first').offset();
	var offsetHighlight = activated.parents('.func-outer').offset();
	jQuery('.ctrl-left .inner-area').getNiceScroll(0).doScrollLeft(offsetHighlight.left - offsetContainer.left, 0);
	jQuery('.ctrl-left .inner-area').getNiceScroll(0).doScrollTop(offsetHighlight.top - offsetContainer.top - 40, 0); // 定位到最佳位置
	setTimeout(function() {
		hideLayer();
	}, 128);// 防止恶意点击
	// 高亮显示特效
	jQuery('.ctrl-left .highlight').removeClass('highlight');
	clearTimeout(intervalID___highlight);
	activated.parents('.func-outer').addClass('highlight');
	intervalID___highlight = setTimeout(function() {
		activated.parents('.func-outer').removeClass('highlight');
	}, 1024);// 较正常点击速率偏高，其中包括滚动条定位的延迟，可获得更佳的用户体验
}

function deselect(tabIndex) {
	showLayer();
	jQuery('.ctrl-right .item-block[tab-index="' + tabIndex + '"]').remove();
	jQuery('.ctrl-right .inner-area').getNiceScroll().resize();
	jQuery('.ctrl-left .func-name[tab-index="' + tabIndex + '"]').parents('.func-outer').removeClass('selected');
	jQuery('.ctrl-left .inner-area').getNiceScroll().resize();
	setTimeout(function() {
		hideLayer();
	}, 128);// 防止恶意点击
}

function expandAll() {
	showLayer();
	jQuery('.ctrl-left .func-outer').each(function() {
		jQuery(this).find('.toggle').removeClass('collapse').addClass('expand');
		var ul = jQuery(this).next('ul');
		if (ul != null && ul.length > 0) {
			ul.show();
		}
	});
	jQuery('.ctrl-left .inner-area').getNiceScroll().resize();
	setTimeout(function() {
		hideLayer();
	}, 128);// 防止恶意点击
}

function collapseAll() {
	showLayer();
	jQuery('.ctrl-left .func-outer').each(function() {
		jQuery(this).find('.toggle').removeClass('expand').addClass('collapse');
		var ul = jQuery(this).next('ul');
		if (ul != null && ul.length > 0) {
			ul.hide();
		}
	});
	jQuery('.ctrl-left .inner-area').getNiceScroll().resize();
	setTimeout(function() {
		hideLayer();
	}, 128);// 防止恶意点击
}

function selectAll() {
	showLayer();
	jQuery('.ctrl-left .func-outer.allow-select').each(function() {
		if (!jQuery(this).hasClass('selected')) {
			jQuery(this).addClass('selected');
			var funcName = jQuery(this).find('.func-name');
			var tabIndex = funcName.attr('tab-index');
			var tabName = funcName.html();
			var itemBlock = '';
			itemBlock += '<div class="item-block" tab-index="' + tabIndex + '"><div class="block-content">';
			itemBlock += '<span class="btn-delete" onclick="deselect(' + tabIndex + ');"></span><span class="text" onclick="linkageCtrlLeft(' + tabIndex + ');">' + tabName + '</span></div></div>';
			jQuery('.ctrl-right .inner-area').append(itemBlock);
		}
	});
	jQuery('.ctrl-right .inner-area').getNiceScroll().resize();
	setTimeout(function() {
		hideLayer();
	}, 128);// 防止恶意点击
}

function deselectAll() {
	showLayer();
	jQuery('.ctrl-right .item-block').remove();
	jQuery('.ctrl-right .inner-area').getNiceScroll().resize();
	jQuery('.ctrl-left .func-outer.selected').removeClass('selected');
	jQuery('.ctrl-left .inner-area').getNiceScroll().resize();
	setTimeout(function() {
		hideLayer();
	}, 128);// 防止恶意点击
}

function saveChange() {
	showLayer();
	var selected = ergodicSelected();// 遍历用户已选择的功能菜单
	if (selected != null && selected != '') {
		var selectedArr = selected.split(',');
		if (selectedArr != null && selectedArr.length > 0X20) {
			if (!confirm('温馨提示：超过' + 0X20 + '个，浏览器可能产生卡顿现象！，是否仍要继续？')) {
				hideLayer();
				return false;
			}
		}
	}
	jQuery('#mainForm\\:configureHidden').val(selected);
	jQuery('#mainForm\\:update').click();
}

function loadSelected() {
	showLayer();
	if (quickStartConfig != null && quickStartConfig != '') {
		var qscArr = quickStartConfig.split(',');
		if (qscArr != null && qscArr.length > 0) {
			for (var i = 0, len = qscArr.length; i < len; i++) {
				var funcName = jQuery('.ctrl-left .func-name[tab-index="' + qscArr[i] + '"]');
				if (funcName != null && funcName.length > 0) {
					var funcOuter = jQuery(funcName.parents('.func-outer'));
					if (funcOuter != null && funcOuter.length > 0) {
						if (funcOuter.hasClass('allow-select') && !funcOuter.hasClass('selected')) {
							funcOuter.addClass('selected');
							var tabIndex = funcName.attr('tab-index');
							var tabName = funcName.html();
							var itemBlock = '';
							itemBlock += '<div class="item-block" tab-index="' + tabIndex + '"><div class="block-content">';
							itemBlock += '<span class="btn-delete" onclick="deselect(' + tabIndex + ');"></span><span class="text" onclick="linkageCtrlLeft(' + tabIndex + ');">' + tabName + '</span></div></div>';
							jQuery('.ctrl-right .inner-area').append(itemBlock);
							jQuery('.ctrl-right .inner-area').getNiceScroll().resize();
						}
					}
				}
			}
		}
	}
	setTimeout(function() {
		hideLayer();
	}, 128);// 防止恶意点击
}

function ergodicSelected() {
	var result = '';
	jQuery('.ctrl-left .inner-area .func-outer.selected').each(function() {
		var funcName = jQuery(this).find('.func-name');
		if (funcName != null && funcName.length > 0) {
			var tabIndex = funcName.attr('tab-index');
			if (tabIndex != null && tabIndex != '') {
				result += tabIndex + ',';
			}
		}
	});
	if (result != null && result != '') {
		result = result.substring(0, result.length - 1);
	}
	return result;
}
