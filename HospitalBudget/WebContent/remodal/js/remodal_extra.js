//准备Remodal的html代码
var _remodal_html = '';
_remodal_html += '<div class="remodal" data-remodal-id="modal_ask" role="dialog" aria-labelledby="modal_ask__title" aria-describedby="modal_ask__desc">';
_remodal_html += '	<button data-remodal-action="close" class="remodal-close" aria-label="Close">';
_remodal_html += '	</button>';
_remodal_html += '	<div>';
_remodal_html += '		<h2 id="modal_ask__title">标题</h2>';
_remodal_html += '		<p id="modal_ask__desc">描述</p>';
_remodal_html += '	</div>';
_remodal_html += '	<br />';
_remodal_html += '	<button data-remodal-action="confirm" class="remodal-confirm">确定</button>';
_remodal_html += '	<button data-remodal-action="cancel" class="remodal-cancel">取消</button>';
_remodal_html += '</div>';
_remodal_html += '<div id="modalMsg" data-remodal-id="modal_msg" role="dialog" aria-labelledby="modal_msg__title"aria-describedby="modal_msg__desc">';
_remodal_html += '	<div>';
_remodal_html += '		<h2 id="modal_msg__title">标题</h2>';
_remodal_html += '		<p id="modal_msg__desc">描述</p>';
_remodal_html += '	</div>';
_remodal_html += '	<br />';
_remodal_html += '	<button data-remodal-action="confirm" class="remodal-confirm">确定</button>';
_remodal_html += '</div>';

// 写入Remodal的html代码
document.write(_remodal_html);

// 绑定Remodal的Events
jQuery(document).on('opening', '.remodal', function() {
    if (___remodal_fn_set['_custom'] != null && ___remodal_fn_set['_custom']['opening'] != null) {
	___remodal_fn_set['_custom']['opening']();
    } else {
	___remodal_fn_set['_default']['opening']();
    }
});

jQuery(document).on('opened', '.remodal', function() {
    if (___remodal_fn_set['_custom'] != null && ___remodal_fn_set['_custom']['opened'] != null) {
	___remodal_fn_set['_custom']['opened']();
    } else {
	___remodal_fn_set['_default']['opened']();
    }
});

jQuery(document).on('closing', '.remodal', function(e) {
    if (___remodal_fn_set['_custom'] != null && ___remodal_fn_set['_custom']['closing'] != null) {
	___remodal_fn_set['_custom']['closing'](e);
    } else {
	___remodal_fn_set['_default']['closing'](e);
    }
});

jQuery(document).on('closed', '.remodal', function(e) {
    if (___remodal_fn_set['_custom'] != null && ___remodal_fn_set['_custom']['closed'] != null) {
	___remodal_fn_set['_custom']['closed'](e);
    } else {
	___remodal_fn_set['_default']['closed'](e);
    }
});

jQuery(document).on('confirmation', '.remodal', function() {
    if (___remodal_fn_set['_custom'] != null && ___remodal_fn_set['_custom']['confirmation'] != null) {
	___remodal_fn_set['_custom']['confirmation']();
    } else {
	___remodal_fn_set['_default']['confirmation']();
    }
});

jQuery(document).on('cancellation', '.remodal', function() {
    if (___remodal_fn_set['_custom'] != null && ___remodal_fn_set['_custom']['cancellation'] != null) {
	___remodal_fn_set['_custom']['cancellation']();
    } else {
	___remodal_fn_set['_default']['cancellation']();
    }
});

// The second way to initialize:
jQuery('[data-remodal-id=modal_msg]').remodal({
    modifier : ''
});
