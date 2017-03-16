var menuInfoJsonArray = null;
var quickStartConfig = null;

jQuery(document).ready(function() {

	jQuery('.exhibition-area').niceScroll({
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
			funcCode += '<li><div class="func-outer' + (node['leaf'].length > 0 || (null == node['tabUrl'] || '' == node['tabUrl']) ? ' no-select' : '') + '"><div class="func-inner">';
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
	jQuery('.function-container').html(funcCode);
	jQuery('.function-container .func-outer').click(function() {
		showMenuMaskLayer();
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
			cache_ul.slideToggle(256, function() {
				hideMenuMaskLayer();
				jQuery('.exhibition-area').getNiceScroll().resize();
			});
		} else {
			if (!jQuery(this).hasClass('no-select')) {
				jQuery(this).toggleClass('selected');
			}
			setTimeout(function() {
				hideMenuMaskLayer();
			}, 256);// 防止恶意点击
		}
	});

});

function loadSelected() {
	if (quickStartConfig != null && quickStartConfig != '') {
		var qscArr = quickStartConfig.split(',');
		if (qscArr != null && qscArr.length > 0) {
			for (var i = 0, len = qscArr.length; i < len; i++) {
				var funcName = jQuery('.function-container .func-outer .func-name[tab-index="' + qscArr[i] + '"]');
				if (funcName != null && funcName.length > 0) {
					var funcOuter = jQuery(funcName.parents('.func-outer'));
					if (funcOuter != null && funcOuter.length > 0) {
						if (!funcOuter.hasClass('selected')) {
							funcOuter.addClass('selected');
						}
					}
				}
			}
		}
	}
}

function ergodicSelected() {
	var result = '';
	jQuery('.function-container .func-outer.selected').each(function() {
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

function showMenuMaskLayer() {
	jQuery('.menu-mask-layer-wrapper .menu-mask-layer-contents').addClass('is-loading');
	jQuery('.menu-mask-layer-wrapper').show();
}

function hideMenuMaskLayer() {
	jQuery('.menu-mask-layer-wrapper .menu-mask-layer-contents').removeClass('is-loading');
	jQuery('.menu-mask-layer-wrapper').hide();
}
