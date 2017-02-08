jQuery(document).ready(function() {
	clientData['requestTime'] = new Date();
	clientData['browserType'] = 'Firefox';
	clientData['userBehaviorAnalysis'] = [ '人造黑洞', '广义相对论', '量子理论', '核聚变', '粒子对撞机', '大爆炸宇宙论' ];
	clientData['ccMessage'] = 'We use cookies to analyse our traffic and to show ads. By using our website, you agree to our use of cookies. ';
	jQuery('input[name="clientData"]').val(JSON.stringify(clientData));

	jQuery('#fileSelector').attr('multiple', 'multiple');

	jQuery('#fileSelector').change(function() {
		jQuery('.selected-files-table .data-row').remove();
		var dataRow = '';
		var type = '';
		var size = 0;
		var name = '';
		for (var i = 0; i < this.files.length; i++) {
			var file = this.files[i];
			dataRow += '<tr class="data-row">';
			dataRow += '	<td></td>';
			type = file.type != null && file.type !== '' && ______sgFileTypeInfo[file.type] != null ? file.type : '?';
			size = file.size;
			name = file.name;
			dataRow += '	<td><img class="icon" src="' + ______sgFileTypeInfo[type].icon + '"></td>';
			dataRow += '	<td>' + ______sgFileTypeInfo[type].type + '</td>';
			dataRow += '	<td>' + size + '字节</td>';
			dataRow += '	<td>' + ___genStdHtml___a_abbr(name) + '</td>';
			dataRow += '	<td></td>';
			dataRow += '</tr>';
			blob = window.URL.createObjectURL(file);
			// ___log(file);
			// ___log(blob);
			// new Audio(blob).play();
			// jQuery('#debug').append('<img src="' + blob + '" width="64"
			// height="64" /><br/>');
		}
		jQuery('.selected-files-table .record').append(dataRow);
		toggleSelectedFilesTable();
	});
	parent.sgFileupload.enableSelectFile();
	parent.sgFileupload.enableClosePanel();
	if (show_panel != null && show_panel != '') {
		parent.sgFileupload.showPanel();
		parent.sgFileupload.doUnlock();
	}
	toggleSelectedFilesTable();
});

function toggleSelectedFilesTable() {
	if (jQuery('.selected-files-table .record .data-row').length > 0) {
		parent.sgFileupload.changeSelectFileText('重新选取');
		parent.sgFileupload.enableUploadFile();
		jQuery('.selected-files-table-none').hide();
		jQuery('.selected-files-table').show();
	} else {
		parent.sgFileupload.changeSelectFileText('选取文件');
		parent.sgFileupload.disableUploadFile();
		jQuery('.selected-files-table').hide();
		jQuery('.selected-files-table-none').css('display', 'block');
	}
}
