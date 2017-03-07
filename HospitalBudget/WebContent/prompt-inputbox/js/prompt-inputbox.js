var ______promptInputbox = {
    wired : false,
    cache : {},
    handleIndex : 0,
    handleCurrent : null
};

jQuery(document).ready(function() {
	if (!______promptInputbox.wired) {
		// 1. 创建节点
		var html = '';
		html += '<div class="prompt-inputbox">';
		html += '	<div class="prompt-inputbox-contents">';
		html += '		<textarea class="textarea-bd"></textarea>';
		html += '		<div class="btn-operation">';
		html += '			<img class="img-yes" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-inputbox/img/icon_success_48x48.png' + '">';
		html += '			<img class="img-no" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-inputbox/img/icon_error_48x48.png' + '">';
		html += '		</div>';
		html += '	</div>';
		html += '</div>';
		jQuery('body').prepend(html);
		// 2. 缓存节点
		______promptInputbox.cache['master'] = jQuery('.prompt-inputbox');
		______promptInputbox.cache['textarea'] = jQuery('.prompt-inputbox .textarea-bd');
		______promptInputbox.cache['imgYes'] = jQuery('.prompt-inputbox .img-yes');
		______promptInputbox.cache['imgNo'] = jQuery('.prompt-inputbox .img-no');
		// 3. 设置滚动条
		______promptInputbox.cache['textarea'].niceScroll({
		    cursorcolor : '#062121',
		    cursorwidth : 8,
		    cursoropacitymax : 0.8,
		    touchbehavior : false,
		    autohidemode : true
		});
		// 4. 绑定事件
		______promptInputbox.cache['textarea'].keypress(function() {
			______promptInputboxNiceScrollResize();
		});
		______promptInputbox.cache['textarea'].keydown(function() {
			______promptInputboxNiceScrollResize();
		});
		______promptInputbox.cache['textarea'].keyup(function() {
			______promptInputboxNiceScrollResize();
		});
		______promptInputbox.cache['textarea'].mouseup(function() {
			______promptInputboxNiceScrollResize();
		});
		______promptInputbox.cache['imgYes'].click(function() {
			______promptInputboxHide();
			jQuery('[prompt-inputbox-handle-index="' + ______promptInputbox.handleCurrent + '"]').val(______promptInputbox.cache['textarea'].val()).focus();
		});
		______promptInputbox.cache['imgNo'].click(function() {
			______promptInputboxHide();
			jQuery('[prompt-inputbox-handle-index="' + ______promptInputbox.handleCurrent + '"]').focus();
		});
		// 5. 标记状态
		______promptInputbox.wired = true;
	}
});

function ______promptInputboxShow() {
	______promptInputbox.cache['master'].show();
	______promptInputbox.cache['textarea'].focus();
}

function ______promptInputboxHide() {
	______promptInputbox.cache['master'].hide();
}

function ______promptInputboxNiceScrollResize() {
	______promptInputbox.cache['textarea'].getNiceScroll().resize();
}

function ___promptInputbox(id) {
	______promptInputbox.handleIndex++;
	var element = document.getElementById(id);
	jQuery(element).attr('prompt-inputbox-handle-index', ______promptInputbox.handleIndex);
	jQuery(element).dblclick(function() {
		______promptInputboxShow();
		______promptInputbox.handleCurrent = jQuery(element).attr('prompt-inputbox-handle-index');
		var eType = element.tagName.toLowerCase();
		if ('input' == eType || 'textarea' == eType) {
			______promptInputbox.cache['textarea'].val(element.value);
		} else {
			alert('无效类型！');
		}
	});
}
