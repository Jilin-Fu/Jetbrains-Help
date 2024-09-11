$(document).ready(function() {
    // Set default headers for AJAX requests
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json'
        }
    });

    // Function to handle submission of license information
    window.submitLicenseInfo = function () {
        let licenseInfo = {
            licenseeName: $('#licenseeName').val(),
            assigneeName: $('#assigneeName').val(),
            expiryDate: $('#expiryDate').val()
        };
        let licenseInfoStr = JSON.stringify(licenseInfo);
        $.post('/updateLicenseInfo', licenseInfoStr).then(response => {
            $('#mask, #form').hide();
            if (response == null || response === '') {
                localStorage.setItem('licenseInfo', licenseInfoStr);
                window.location.reload()
            } else {
                customAlert(response);
            }
        });
    };

    // Function to handle search input
    $('#search').on('input', function(e) {
        $("#product-list").load('/search?search=' + e.target.value);
    });

    // Function to show license form
    window.showLicenseForm = function () {
        let licenseInfo = JSON.parse(localStorage.getItem('licenseInfo'));
        if (licenseInfo != null) {
            $('#licenseeName').val(licenseInfo?.licenseeName || 'default');
            $('#assigneeName').val(licenseInfo?.assigneeName || 'default');
            $('#expiryDate').val(licenseInfo?.expiryDate || '2026-08-16');
        }
        $('#mask, #form').show();
    };

    // Function to show VM options
    window.showVmoptins = function () {
        var text = "-javaagent:/(Your Path)/ja-netfilter/ja-netfilter.jar\n" +
        "--add-opens=java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED\n" +
        "--add-opens=java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED";
        navigator.clipboard.writeText(text).then(function() {
            customAlert("已复制：\n" + text);
        }).catch(function(error) {
            customAlert("复制失败: " + error);
        });
    };

    // Function to copy license
    window.copyLicense = async function (e) {
        while (localStorage.getItem('licenseInfo') === null) {
            $('#mask, #form').show();
            await new Promise(r => setTimeout(r, 1000));
        }
        let licenseInfo = JSON.parse(localStorage.getItem('licenseInfo'));
        let productCode = $(e).closest('.card').data('productCodes');
        let data = {
            "licenseeName": licenseInfo.licenseeName,
            "assigneeName": licenseInfo.assigneeName,
            "expiryDate": licenseInfo.expiryDate,
            "productCode": productCode,
        };
        $.post('/generateLicense', JSON.stringify(data))
            .then(response => {
                copyText(response)
                    .then((result) => {
                        customAlert(result);
                    });
            });
    };

// Function to copy text to clipboard
    const copyText = async (val) => {
        if (navigator.clipboard && navigator.permissions) {
            await navigator.clipboard.writeText(val);
            return "The activation code has been copied";
        } else {
            console.log(val);
            return "The system does not support it, please go to the console to copy it manually";
        }
    };

});

// 自定义 alert 函数
function customAlert(message) {
    // 替换换行符为 <br>
    let formattedMessage = message.replace(/\n/g, '<br>');

    // 设置消息内容
    $('#alert-message').html(formattedMessage);

    // 显示弹框
    $('#custom-alert').fadeIn();

    // 点击OK按钮关闭弹框
    $('#alert-ok').on('click', function () {
        $('#custom-alert').fadeOut();
    });
}

function closeForm() {
    $('#mask, #form').hide();
}

