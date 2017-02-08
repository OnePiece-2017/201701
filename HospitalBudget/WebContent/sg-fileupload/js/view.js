jQuery(document).ready(function() {
	parent.sgFileupload.updateUploaded = updateUploaded;
	parent.sgFileupload.loadUploaded = loadUploaded;
	toggleSelectedFilesTable();
});

function toggleSelectedFilesTable() {
	if (jQuery('.uploaded-files-table .record .data-row').length > 0) {
		jQuery('.uploaded-files-table-none').hide();
		jQuery('.uploaded-files-table').show();
	} else {
		jQuery('.uploaded-files-table').hide();
		jQuery('.uploaded-files-table-none').css('display', 'block');
	}
}

function updateUploaded(result) {
	jQuery('.uploaded-files-table .record .data-row').removeClass('recently');
	if (result != null && 'INVOKE_SUCCESS' == result.invoke_result) {
		var items = result.data;
		if (items != null && items.length > 0) {
			parent.sgFileupload.setStorageChanged(parent.sgFileupload.getCurrentTarget(), true);
			var dataRow = '';
			var type = '';
			var size = 0;
			var name = '';
			var md5Hex = '';
			for (var i = 0; i < items.length; i++) {
				if (items[i] != null) {
					dataRow = '';
					var file = items[i];
					dataRow += '<tr class="data-row recently">';
					dataRow += '	<td></td>';
					type = file.type != null && file.type !== '' && ______sgFileTypeInfo[file.type] != null ? file.type : '?';
					size = file.size;
					name = file.name;
					md5Hex = file.md5Hex;
					dataRow += '	<td><img class="icon" src="' + ______sgFileTypeInfo[type].icon + '"></td>';
					dataRow += '	<td>' + ______sgFileTypeInfo[type].type + '</td>';
					dataRow += '	<td>' + size + '字节</td>';
					dataRow += '	<td>' + ___genStdHtml___a_abbr(name) + '</td>';
					dataRow += '	<td>';
					dataRow += '		<a class="a-btn-download" onclick="downloadFile(this);" title="下载"><img src="../misc/icon_down_32x32.png"></a>';
					dataRow += '		<a class="a-btn-delete" onclick="deleteDataRow(this);" title="删除"><img src="../misc/icon_delete_sign_32x32.png"></a>';
					dataRow += '	</td>';
					dataRow += '	<td></td>';
					dataRow += '</tr>';
					jQuery('.uploaded-files-table .record .data-row-reference-point').after(dataRow);
					parent.sgFileupload.getStorageItems(parent.sgFileupload.getCurrentTarget()).push({
					    name : name,
					    type : type,
					    size : size,
					    md5Hex : md5Hex
					});
				}
			}
		}
		jQuery('body').getNiceScroll().resize();
		jQuery('body').getNiceScroll(0).doScrollLeft(0, 0);
		jQuery('body').getNiceScroll(0).doScrollTop(0, 0);
		toggleSelectedFilesTable();
		if (top.toastr != null) {
			top.toastr.options = {
			    'closeButton' : false,
			    'debug' : false,
			    'newestOnTop' : false,
			    'progressBar' : false,
			    'rtl' : false,
			    'positionClass' : 'toast-top-full-width',
			    'preventDuplicates' : false,
			    'onclick' : null,
			    'showDuration' : 300,
			    'hideDuration' : 1000,
			    'timeOut' : 3000,
			    'extendedTimeOut' : 1000,
			    'showEasing' : 'swing',
			    'hideEasing' : 'linear',
			    'showMethod' : 'fadeIn',
			    'hideMethod' : 'fadeOut'
			};
			top.toastr['info']('温馨提示', '上传成功！');
		} else {
			alert('上传成功！');
		}
		parent.sgFileupload.reloadController();
	}
}

function loadUploaded(items) {
	jQuery('.uploaded-files-table .record .data-row').remove();
	if (items != null && items.length > 0) {
		var dataRow = '';
		var type = '';
		var size = 0;
		var name = '';
		for (var i = 0; i < items.length; i++) {
			if (items[i] != null) {
				dataRow = '';
				var file = items[i];
				dataRow += '<tr class="data-row">';
				dataRow += '	<td></td>';
				type = file.type != null && file.type !== '' && ______sgFileTypeInfo[file.type] != null ? file.type : '?';
				size = file.size;
				name = file.name;
				dataRow += '	<td><img class="icon" src="' + ______sgFileTypeInfo[type].icon + '"></td>';
				dataRow += '	<td>' + ______sgFileTypeInfo[type].type + '</td>';
				dataRow += '	<td>' + size + '字节</td>';
				dataRow += '	<td>' + ___genStdHtml___a_abbr(name) + '</td>';
				dataRow += '	<td>';
				dataRow += '		<a class="a-btn-download" onclick="downloadFile(this);" title="下载"><img src="../misc/icon_down_32x32.png"></a>';
				dataRow += '		<a class="a-btn-delete" onclick="deleteDataRow(this);" title="删除"><img src="../misc/icon_delete_sign_32x32.png"></a>';
				dataRow += '	</td>';
				dataRow += '	<td></td>';
				dataRow += '</tr>';
				jQuery('.uploaded-files-table .record .data-row-reference-point').after(dataRow);
			}
		}
	}
	jQuery('body').getNiceScroll().resize();
	jQuery('body').getNiceScroll(0).doScrollLeft(0, 0);
	jQuery('body').getNiceScroll(0).doScrollTop(0, 0);
	toggleSelectedFilesTable();
}

function downloadFile(item) {
	if (!parent.sgFileupload.getTriggerLock('download')) {
		parent.sgFileupload.setTriggerLock('download', true);
		jQuery('.uploaded-files-table .record .a-btn-download').each(function(index) {
			if (this === item)
				parent.sgFileupload.fileDownload(JSON.stringify(parent.sgFileupload.getStorageItems(parent.sgFileupload.getCurrentTarget())[parent.sgFileupload.getStorageItems(parent.sgFileupload.getCurrentTarget()).length - index - 1]));
		});
		setTimeout(function() {
			parent.sgFileupload.setTriggerLock('download', false);
		}, 3000);
	} else {
		var message = '您的下载动作过于频繁，请稍后重试！';
		if (top.toastr != null) {
			top.toastr.options = {
			    'closeButton' : false,
			    'debug' : false,
			    'newestOnTop' : false,
			    'progressBar' : false,
			    'rtl' : false,
			    'positionClass' : 'toast-top-full-width',
			    'preventDuplicates' : false,
			    'onclick' : null,
			    'showDuration' : 300,
			    'hideDuration' : 1000,
			    'timeOut' : 3000,
			    'extendedTimeOut' : 1000,
			    'showEasing' : 'swing',
			    'hideEasing' : 'linear',
			    'showMethod' : 'fadeIn',
			    'hideMethod' : 'fadeOut'
			};
			top.toastr['error']('', message);
		} else {
			alert(message);
		}
	}
}

function deleteDataRow(item) {
	if (confirm('是否删除？')) {
		jQuery('.uploaded-files-table .record .a-btn-delete').each(function(index) {
			if (this === item)
				parent.sgFileupload.getStorageItems(parent.sgFileupload.getCurrentTarget()).splice(parent.sgFileupload.getStorageItems(parent.sgFileupload.getCurrentTarget()).length - index - 1, 1);
		});
		jQuery(item).parents('tr.data-row').remove();
		parent.sgFileupload.setStorageChanged(parent.sgFileupload.getCurrentTarget(), true);
		toggleSelectedFilesTable();
	}
}
