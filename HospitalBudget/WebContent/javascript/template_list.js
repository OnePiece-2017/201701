var ______sgTemplate = {
    wired : false,
    cache : {}
};

jQuery(document).ready(function() {
	if (!______sgTemplate.wired) {
		// 1. 创建节点
		jQuery('body').prepend('<div class="sg-mask-layer-wrapper"><div class="sg-mask-layer-contents"></div></div>');
		// 2. 标记状态
		______sgTemplate.wired = true;
	}

	___trash = 'list';
});

// 页面跳转
function gotoPage(pageCount) {
	___dynamic_function = function() {
		jQuery('#pageIndex').focus();
	};
	// 验证-页码
	var pageIndex = trimValObj(jQuery('#pageIndex'));
	if (pageIndex == null || pageIndex == '') {
		___msg('温馨提示', '请输入页码！', {
			confirmation : ___dynamic_function
		});
		return false;
	}
	if (isNaN(pageIndex) || (pageIndex.indexOf('.') != -1) || pageIndex <= 0) {
		___msg('温馨提示', '无效的页码！', {
			confirmation : ___dynamic_function
		});
		return false;
	}
	if (pageIndex.length > 2 && (pageIndex.substr(0, 2) == '0x' || pageIndex.substr(0, 2) == '0X')) {
		___msg('温馨提示', '非法的页码！', {
			confirmation : ___dynamic_function
		});
		return false;
	}
	if (pageIndex < 1 || pageIndex > pageCount) {
		___msg('温馨提示', '请输入1到' + pageCount + '之间的页码！', {
			confirmation : ___dynamic_function
		});
		return false;
	}
	var url = jQuery('#gotoPageLink').attr('href');// 分为4种情况：无、前、中、后
	if (url != null && url != '') {
		var urlArr = url.split('?');
		if (urlArr != null && urlArr.length > 1) {
			var argsArr = urlArr[1].split('&');
			if (argsArr != null && argsArr.length > 0) {
				var has_page = false, index = 0;
				for (index = 0; index < argsArr.length; index++) {
					if (argsArr[index].split('=')[0] == 'page') {
						has_page = true;
						argsArr[index] = 'page=' + pageIndex;
					}
				}
				if (!has_page) {
					argsArr[index] = 'page=' + pageIndex;
				}
				urlArr[1] = argsArr.join('&');
				jQuery('#gotoPageLink').attr('href', urlArr.join('?'));
			}
		}
	}
}

// 去除左右空白字符，仅限可调用val()的object
function trimValObj(object) {
	object.val(jQuery.trim(object.val()));
	return object.val();
}

function showLayer() {
	jQuery('.sg-mask-layer-wrapper .sg-mask-layer-contents').addClass('is-loading');
	jQuery('.sg-mask-layer-wrapper').show();
}

function hideLayer() {
	jQuery('.sg-mask-layer-wrapper .sg-mask-layer-contents').removeClass('is-loading');
	jQuery('.sg-mask-layer-wrapper').hide();
}

function wrapQueryResults() {
	if (jQuery('.g-data-list-table').length > 0) {
		if (0 == jQuery('.g-data-list-table .empty-record').length) {
			var html = '';
			html += '<div class="empty-record">';
			html += '	<img class="icon" src="../images/icon_info_32x32.png">';
			html += '	<strong class="message">暂无数据！</strong>';
			html += '</div>';
			jQuery('.g-data-list-table').prepend(html);
		}
		if (jQuery('.g-data-list-table .node-record').length > 0) {
			jQuery('.g-data-list-table .empty-record').hide();
			jQuery('.g-data-list-table .record, .g-data-list-table .pagination-model').show();
		} else {
			jQuery('.g-data-list-table .record, .g-data-list-table .pagination-model').hide();
			jQuery('.g-data-list-table .empty-record').show();
		}
	}
}
