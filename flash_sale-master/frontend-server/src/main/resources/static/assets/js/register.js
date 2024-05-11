//  用户注册操作
layui.form.on('submit(userRegister)', function (res) {
    var formVal = res.field
    var currentParams = {
        phone: formVal.phone,
        password: formVal.passWord
    }
    if (!window.localStorage) {
        alert("浏览器不支持localStorage");
        return false;
    } else {
        ajaxHttp({
            url: '/uaa/register',
            type: 'POST',
            data: JSON.stringify(currentParams),
        }, function (params) {
            window.localStorage.setItem("token", params.data.token);
            window.localStorage.setItem("userInfo", JSON.stringify(params.data.userInfo));
            window.location.href = "/login.html";
        });
    }
    return false;
})