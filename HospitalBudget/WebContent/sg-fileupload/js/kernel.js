var ______sgFileupload = {
    installation : {
        succeed : true,
        message : null,
        notified : false
    },
    isWired : false,
    isReady : false,
    isLocked : false,
    isException : false,
    storage : {},
    currentTarget : null,
    exceptionMessage : 'sgFileupload获取关联数据失败！',
    triggerLock : {
        ClosePanel : false,
        download : false
    },
    callback : {
        saveData : null,
        completed : null
    }
};

(function(define) {
	define([ 'jquery' ], function($) {
		return (function() {
			return {
			    install : install,
			    uninstall : uninstall,
			    reinstall : reinstall,
			    showPanel : showPanel,
			    showMaskLayer : showMaskLayer,
			    hideMaskLayer : hideMaskLayer,
			    enableSelectFile : enableSelectFile,
			    disableSelectFile : disableSelectFile,
			    enableUploadFile : enableUploadFile,
			    disableUploadFile : disableUploadFile,
			    enableClosePanel : enableClosePanel,
			    disableClosePanel : disableClosePanel,
			    changeSelectFileText : changeSelectFileText,
			    doUnlock : doUnlock,
			    updateUploaded : null,
			    loadUploaded : null,
			    reloadController : reloadController,
			    getCurrentTarget : getCurrentTarget,
			    getStorageHolder : getStorageHolder,
			    getStorageItems : getStorageItems,
			    getSource : getSource,
			    getCallback : getCallback,
			    setCallback : setCallback,
			    getStorageChanged : getStorageChanged,
			    setStorageChanged : setStorageChanged,
			    fileDownload : fileDownload,
			    reloadDownload : reloadDownload,
			    getTriggerLock : getTriggerLock,
			    setTriggerLock : setTriggerLock
			};

			function validateArguments(arg) {
				return arg != null && 'object' === typeof arg && !arg.hasOwnProperty('length') && 'target' in arg && 'alias' in arg && 'source' in arg;
			}

			function install(arg) {
				if (validateArguments(arg)) {
					var target = arg['target'], alias = 'string' === typeof arg['alias'] && arg['alias'] != null && arg['alias'] !== '' ? arg['alias'] : 'NONAME', source = arg['source'];
					if (!______sgFileupload.isWired) {
						var panelHtml = '';
						panelHtml += '<div class="sg-fileupload-panel">';
						panelHtml += '	<div class="mask-layer">';
						panelHtml += '		<img src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/sg-fileupload/ani/ajax.gif" />';
						panelHtml += '	</div>';
						panelHtml += '	<div class="banner">';
						panelHtml += '		<strong class="alias"></strong>';
						panelHtml += '		<strong class="select-file">选择文件</strong>';
						panelHtml += '		<strong class="upload-file">上传</strong>';
						panelHtml += '		<img class="close-panel" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/sg-fileupload/img/icon_delete_sign.png" />';
						panelHtml += '	</div>';
						panelHtml += '	<div class="prepared">';
						panelHtml += '		<iframe id="sg-fileupload-iframe-controller" width="100%" height="100%" style="border: none;" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/sg-fileupload/page/Controller.seam"></iframe>';
						panelHtml += '	</div>';
						panelHtml += '	<div class="centre">';
						panelHtml += '	</div>';
						panelHtml += '	<div class="uploaded">';
						panelHtml += '		<iframe id="sg-fileupload-iframe-view" width="100%" height="100%" style="border: none;" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/sg-fileupload/page/View.seam"></iframe>';
						panelHtml += '	</div>';
						panelHtml += '	<div class="download">';
						panelHtml += '		<iframe id="sg-fileupload-iframe-download" width="100%" height="100%" style="border: none;" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/sg-fileupload/page/Download.seam"></iframe>';
						panelHtml += '	</div>';
						panelHtml += '	<div class="bottom">';
						panelHtml += '	</div>';
						panelHtml += '</div>';
						$('body').prepend(panelHtml);
						$('.sg-fileupload-panel .banner .select-file').click(function() {
							if (!$(this).hasClass('disabled')) {
								$('#sg-fileupload-iframe-controller').contents().find('#fileSelector').click();
							}
						});
						$('.sg-fileupload-panel .banner .upload-file').click(function() {
							if (!$(this).hasClass('disabled')) {
								showMaskLayer();
								$('.sg-fileupload-panel .banner .select-file, .upload-file, .close-panel').addClass('disabled');
								$('#sg-fileupload-iframe-controller').contents().find('#btnUpload').click();
							}
						});
						$('.sg-fileupload-panel .banner .close-panel').click(function() {
							if (!$(this).hasClass('disabled')) {
								if (!______sgFileupload.triggerLock.ClosePanel) {
									______sgFileupload.triggerLock.ClosePanel = true;
									if (______sgFileupload.storage[______sgFileupload.currentTarget]['changed']) {
										showMaskLayer();
										execCallback('saveData');
										var args = {
										    'assist_key' : 'save_data',
										    'assist_value' : ______sgFileupload.storage[______sgFileupload.currentTarget]
										};
										$.ajax({
										    type : 'POST',
										    dataType : 'jsonp',
										    jsonp : 'callback',
										    url : (___contextRoot != '/' ? ___contextRoot : '') + '/FileUploadAssistServlet',
										    data : {
											    'args' : JSON.stringify(args)
										    },
										    success : function(result) {
											    if (result != null && 'INVOKE_SUCCESS' == result.invoke_result) {
												    ______sgFileupload.storage[______sgFileupload.currentTarget]['changed'] = false;
												    var message = '保存数据成功！';
												    message += (top.toastr != null ? '<br>' : '\n') + '别名〔' + ______sgFileupload.storage[______sgFileupload.currentTarget]['alias'] + '〕';
												    //改变附件颜色
												    jQuery("#" + target).css("color","blue");
												    //将附件名字显示出来
												    var fileNames = "";
												    var file_item = ______sgFileupload.storage[______sgFileupload.currentTarget]['items'];
												    for (var i = 0; i < file_item.length; i++) {
												    	fileNames +=  file_item[i].name+ ",";
												    }
												    if(fileNames != "" && fileNames.length > 0){
												    	fileNames = fileNames.substring(0, fileNames.length-1);
												    }
												    jQuery("#file_name").html(fileNames);
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
													    top.toastr['success']('', message);
												    } else {
													    alert(message);
												    }
												    $('.sg-fileupload-panel').fadeOut('fast', function() {
													    hideMaskLayer();
												    });
											    } else {
												    var message = '保存数据失败！';
												    message += (top.toastr != null ? '<br>' : '\n') + '别名〔' + ______sgFileupload.storage[______sgFileupload.currentTarget]['alias'] + '〕';
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
												    $('.sg-fileupload-panel').fadeOut('fast', function() {
													    hideMaskLayer();
												    });
											    }
											    ______sgFileupload.triggerLock.ClosePanel = false;
										    },
										    error : function(err) {
											    hideMaskLayer();
											    $('.sg-fileupload-panel').fadeOut();
											    alert('FileUploadAssistServlet请求失败！');
											    ______sgFileupload.triggerLock.ClosePanel = false;
										    }
										});
									} else {
										$('.sg-fileupload-panel').fadeOut();
										______sgFileupload.triggerLock.ClosePanel = false;
									}
								}
							}
						});
						______sgFileupload.isWired = true;
					}

					var element = document.getElementById(target);
					if (element != null) {
						if (______sgFileupload.storage[target] != null) {
							______sgFileupload.installation.succeed = false;
							______sgFileupload.installation.message = 'sgFileupload安装失败！重复的目标元素〔' + target + '〕';
						} else {
							______sgFileupload.storage[target] = {};
							______sgFileupload.storage[target]['ready'] = false;
							______sgFileupload.storage[target]['alias'] = alias;
							______sgFileupload.storage[target]['changed'] = false;
							var args = {
							    'assist_key' : 'gain_specified_uploaded',
							    'assist_value' : source
							};
							$.ajax({
							    type : 'POST',
							    dataType : 'jsonp',
							    jsonp : 'callback',
							    url : (___contextRoot != '/' ? ___contextRoot : '') + '/FileUploadAssistServlet',
							    data : {
								    'args' : JSON.stringify(args)
							    },
							    success : function(result) {
								    if (result != null && 'INVOKE_SUCCESS' == result.invoke_result) {
									    ______sgFileupload.storage[target]['items'] = result.data.items;
									    ______sgFileupload.storage[target]['source'] = result.data.source;
									    ______sgFileupload.storage[target]['rebuild'] = result.data.rebuild;
									    $(element).addClass('sg-fileupload-attachment');
									    if ('class' in arg && 'string' === typeof arg['class'] && arg['class'] != null && arg['class'] !== '') {
										    $(element).addClass(arg['class']);
									    } else if (element.tagName != null && element.tagName !== '' && 'span' == element.tagName.toLowerCase()) {
										    $(element).addClass('preset');
									    }
									    setCallback('completed', arg['completed']);
									    $(element).click(function() {
										    if (!______sgFileupload.isLocked) {
											    ______sgFileupload.isLocked = true;
											    $('.sg-fileupload-panel .banner .alias').html(______sgFileupload.storage[target]['alias']);
											    ______sgFileupload.currentTarget = target;
											    if (______sgFileupload.isReady) {
												    reloadController();
												    sgFileupload.loadUploaded(______sgFileupload.storage[target]['items']);
											    } else {
												    var approved = true;
												    for ( var key in ______sgFileupload.storage) {
													    if (!(approved = ______sgFileupload.storage[key]['ready'])) {
														    break;
													    }
												    }
												    if (approved) {
													    reloadController();
													    setTimeout(function() {
														    sgFileupload.loadUploaded(______sgFileupload.storage[target]['items']);
														    ______sgFileupload.isReady = true;
													    }, 1024);// 等待view.js设置loadUploaded
												    } else {
													    var message = ______sgFileupload.isException ? ______sgFileupload.exceptionMessage : 'sgFileupload未就绪！';
													    if (top.toastr != null) {
														    top.toastr.options = {
														        'closeButton' : false,
														        'debug' : false,
														        'newestOnTop' : false,
														        'progressBar' : false,
														        'rtl' : false,
														        'positionClass' : 'toast-top-full-width',
														        'preventDuplicates' : true,
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
														    top.toastr[______sgFileupload.isException ? 'error' : 'info']('', message);
													    } else {
														    alert(message);
													    }
													    doUnlock();
												    }
											    }
										    }
									    });
									    ______sgFileupload.storage[target]['ready'] = true;
									    execCallback('completed');
								    } else {
									    if (!______sgFileupload.isException) {
										    ______sgFileupload.isException = true;
									    }
									    ______sgFileupload.exceptionMessage += (top.toastr != null ? '<br>' : '\n') + '别名〔' + ______sgFileupload.storage[target]['alias'] + '〕';
								    }
							    },
							    error : function(err) {
								    alert('gain_specified_uploaded error!');
							    }
							});
						}
						if (!______sgFileupload.installation.succeed) {
							if (!______sgFileupload.installation.notified) {
								______sgFileupload.installation.notified = true;
								alert(______sgFileupload.installation.message);
							}
							return false;
						}
					} else {
						alert('sgFileupload安装失败！无效的目标元素〔' + target + '〕');
						return false;
					}
				} else {
					alert('sgFileupload安装失败！无效参数！');
					return false;
				}
			}

			function uninstall(target) {
				delete ______sgFileupload.storage[target];
			}

			function reinstall(arg) {
				if (validateArguments(arg)) {
					uninstall(arg['target']);
					return install(arg);
				} else {
					alert('sgFileupload安装失败！无效参数！');
					return false;
				}
			}

			function doUnlock() {
				______sgFileupload.isLocked = false;
			}

			function showPanel() {
				$('.sg-fileupload-panel').fadeIn('fast');
			}

			function showMaskLayer() {
				$('.sg-fileupload-panel .mask-layer').show();
			}

			function hideMaskLayer() {
				$('.sg-fileupload-panel .mask-layer').hide();
			}

			function enableSelectFile() {
				$('.sg-fileupload-panel .banner .select-file').removeClass('disabled');
			}

			function disableSelectFile() {
				$('.sg-fileupload-panel .banner .select-file').addClass('disabled');
			}

			function enableUploadFile() {
				$('.sg-fileupload-panel .banner .upload-file').removeClass('disabled');
			}

			function disableUploadFile() {
				$('.sg-fileupload-panel .banner .upload-file').addClass('disabled');
			}

			function enableClosePanel() {
				$('.sg-fileupload-panel .banner .close-panel').removeClass('disabled');
			}

			function disableClosePanel() {
				$('.sg-fileupload-panel .banner .close-panel').addClass('disabled');
			}

			function changeSelectFileText(text) {
				$('.sg-fileupload-panel .banner .select-file').html(text);
			}

			function reloadController() {
				$('#sg-fileupload-iframe-controller').attr('src', (___contextRoot != '/' ? ___contextRoot : '') + '/sg-fileupload/page/Controller.seam?show_panel=yes');
			}

			function getCurrentTarget() {
				return ______sgFileupload.currentTarget;
			}

			function getStorageHolder() {
				return ______sgFileupload.storage;
			}

			function getStorageItems(target) {
				return ______sgFileupload.storage[target]['items'];
			}

			function getSource(target) {
				return ______sgFileupload.storage[target] != null ? ______sgFileupload.storage[target]['source'] : null;
			}

			function getCallback() {
				return ______sgFileupload.callback;
			}

			function setCallback(which, handle) {
				if (______sgFileupload.callback.hasOwnProperty(which) && 'function' === typeof handle) {
					______sgFileupload.callback[which] = handle;
				}
			}

			function getStorageChanged(target) {
				return ______sgFileupload.storage[target]['changed'];
			}

			function setStorageChanged(target, changed) {
				______sgFileupload.storage[target]['changed'] = changed;
			}

			function execCallback(which) {
				if (______sgFileupload.callback.hasOwnProperty(which) && 'function' === typeof ______sgFileupload.callback[which]) {
					______sgFileupload.callback[which]();
				}
			}

			function fileDownload(args) {
				$('#sg-fileupload-iframe-download').contents().find('input[name="args"]').val(args);
				$('#sg-fileupload-iframe-download').contents().find('#btnDownload').click();
			}

			function reloadDownload() {
				$('#sg-fileupload-iframe-download').attr('src', (___contextRoot != '/' ? ___contextRoot : '') + '/sg-fileupload/page/Download.seam');
			}

			function getTriggerLock(key) {
				return ______sgFileupload.triggerLock[key];
			}

			function setTriggerLock(key, value) {
				______sgFileupload.triggerLock[key] = value;
			}

		})();
	});
}(typeof define === 'function' && define.amd ? define : function(deps, factory) {
	if (typeof module !== 'undefined' && module.exports) {
		module.exports = factory(require('jquery'));
	} else {
		window.sgFileupload = factory(window.jQuery);
	}
}));
