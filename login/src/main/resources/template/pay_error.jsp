<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setCharacterEncoding("utf-8");%>

<!DOCTYPE html>
<!-- saved from url=(0116)https://paycenter.chuxinhudong.com/v1/web/nowPayWeb/tlbbrybwx/wechat/wx7c6587fcbdf80d21/oPk_15IWalBfSgeQ3hG3p9bnRTqY -->
<html lang="en" style="font-size: 512px;"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>请求异常</title>
</head>

<body>
    
    
        <div class="rechar_div">
            <div class="rechar_title"></div>
        </div>
    
    <div class="rechar_main">
            
            <div class="rechar_main_imgs"></div>
            <div class="rechar_main_test"><%=request.getAttribute("info")%></div>
        
    </div>

<script>
    (function () {
        
        var baseFontSize = 100;
        var baseWidth = 375;

        var set = function () {
            var clientWidth = document.documentElement.clientWidth || window.innerWidth;

            var rem = 100;
            if (clientWidth != baseWidth) {
                rem = Math.floor(clientWidth / baseWidth * baseFontSize);
            }

            document.querySelector('html').style.fontSize = rem + 'px';
        }
        set();

        window.addEventListener('resize', set);
    }());
</script>
<style>
    * {
        font-size: 0.16rem;
        margin: 0;
        padding: 0;
        box-sizing: border-box;
        font-family: "微软雅黑 Bold", "微软雅黑";
    }
	
	.rechar_div {
    width: 100vw;
    height: 0.5rem;
    background-color: #FE7F4C;
    color: white;
}

    .rechar_title {
        width: 100%;
        height: 0.5rem;
        line-height: 0.5rem;
        text-align: center;
        font-size: 0.18rem;
        position: relative;
    }

    .rechar_main {
        width: 100vw;
        height: calc(100vh - 0.5rem);
        background-color: white;
        border: 1px solid #ddd;
    }

    .rechar_main_test {
        font-size: 0.16rem;
        margin: auto;
        text-align: center;
        margin-top: 0.3rem;
    }

    .rechar_main_imgs {
        width: 0.59rem;
        height: 0.59rem;
        background: url(pay_error.png) no-repeat center center;
        background-size: 100% 100%;
        margin: auto;
        margin-top: 1rem;
    }
</style>

</body></html>