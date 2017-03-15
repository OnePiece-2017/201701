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

	___trash = 'edit';
});

function showLayer() {
	jQuery('.sg-mask-layer-wrapper .sg-mask-layer-contents').addClass('is-loading');
	jQuery('.sg-mask-layer-wrapper').show();
}

function hideLayer() {
	jQuery('.sg-mask-layer-wrapper .sg-mask-layer-contents').removeClass('is-loading');
	jQuery('.sg-mask-layer-wrapper').hide();
}
