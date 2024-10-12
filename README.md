# JDX


> 该库为个人定制化修改，如需使用请联系，文档不做更新   
> 以下为原作者文档，仅供参考   

## 📢 特别声明:

1. 本仓库涉及的任何解锁和解密分析脚本或代码，仅用于测试和学习研究，禁止用于商业用途，不能保证其合法性，准确性，完整性和有效性，请根据情况自行判断.
2. 请勿将本项目的任何内容用于商业或非法目的，否则后果自负.
3. 如果任何单位或个人认为该项目的脚本可能涉嫌侵犯其权利，则应及时通知并提供身份证明，所有权证明，我们将在收到认证文件后删除相关代码.
4. 任何以任何方式查看此项目的人或直接或间接使用本仓库项目的任何脚本的使用者都应仔细阅读此声明。JDX 保留随时更改或补充此免责声明的权利。一旦使用并复制了任何本仓库项目的规则，则视为您已接受此免责声明.
5. 您必须在下载后的24小时内从计算机或手机中完全删除以上内容.
6. 您使用或者复制了本仓库且本人制作的任何脚本，则视为已接受此声明，请仔细阅读

## 🐳 安装说明

本项目已打包成`docker`镜像，拉取配置即可使用
> docker安装方法不再赘述

### 1. 拉取并运行docker

```dockerfile
docker run -d \
    -v <config dir>:/jdx/config \
    -p <port>:80 \
    --restart=always \
    --name jdx registry.cn-hangzhou.aliyuncs.com/yiidii-hub/jdx:v0.2.2
```
> 这里命令自行替换卷和端口映射
> 
> 例如：
> ```dockerfile
> docker run -d \
>   -v  /data/jdx/config:/jdx/config \
>   -p 5702:80 \
>   --restart=always \
>   --name jdx registry.cn-hangzhou.aliyuncs.com/yiidii-hub/jdx:v0.2.2
> ```
> 
注意：
 - 记得放行端口

### 2. 前台访问
这时候访问 `http://ip:port/` 就能访问了

### 3. 后台登录
<p style="color: red; font-size: 32px"> 所有涉及配置修改的都在后台管理操作并修改，侧滑即可（不要在修改文件了！！！） </p>
<p style="color: red; font-size: 32px"> 所有涉及配置修改的都在后台管理操作并修改，侧滑即可（不要在修改文件了！！！） </p>
<p style="color: red; font-size: 32px"> 所有涉及配置修改的都在后台管理操作并修改，侧滑即可（不要在修改文件了！！！） </p>
后台地址： `http://ip:port/admin`
首次登录用户名：`admin`, 密码：`123465`, **千万记得修改密码！！！！！**

## 📌 一对一推送
 脚本参考[ccwav/QLScript2](https://github.com/ccwav/QLScript2) ，直接拉库即可

--------------
**接下来就是一对一的配置**

1. 这里一对一的配置需要`wxPusher`的`appToken`，在`wxPusher`后台修改应用回调地址（`wxPusher后台` -> `应用管理` -> `应用信息` -> `事件回调地址`），修改格式如下
    `http://ip:port/api/third/wxPusher/follow/callback`
    > 这里的`ip:port`是`JDX`的`ip:port`
   
    原理是：在扫码获取Cookie的时候，会根据pt_pin生成一个二维码，用户扫码关注后，`wxPusher`会调用`JDX`的该接口，JDX会在青龙备注修改用户的`UID`，格式为`ccwav`采用通知的格式，`@@`分割。


2. 在青龙后台配置`wxPusher`的`appToken`
    ```javascript
    export WP_APP_TOKEN_ONE="AT_xxx"
    ```

## 财富岛
`http://ip:port/api/cfd`
在青龙新增环境变量`JDX_CFD_COOKIE`，值为`pt_key=xxx;pt_pin=xxx;`即可，备注上`uid`即可推送

## Q&A
1. **为什么扫码了，没有绑定上uid？**

    出现这种问题的一般是中文的`pt_pin`，自己尝试在青龙后台，环境变量的搜索框里面，用自己的`pt_pin`搜索一下，是不是能搜索出来你的Cookie，据测试是低版本的青龙无法搜索出来，**建议升级青龙版本**.
2. **获取二维码提示什么什么频繁，24小时后重试**

    是京东返回的，那就24小时后重试咯。**建议测试的时候点击`已有Cookie？`按钮测试**
3. **无法正常生成二维码**

    在`JDX`后台配置`wxPusher`的`appToken`
4. **都配置正常了，没有一对一推送**

    这里建议看下[ccwav/QLScript2](https://github.com/ccwav/QLScript2) 的设置说明
    
    另外，青龙和ccwav都有说明：
    > ql repo 会默认复制 deps 目录下的文件到仓库目录，所以你要固定你的 sendNotify 或者其他文件时，只需要把文件放到deps目录即可
5. **青龙连接失败**

   建议新增新的青龙client来使用（新的`cliendId`和新的`cliendSecret`）
## 🥂 更新说明
1. 停止并删除容器
```shell
docker stop jdx && docker rm jdx
```
2. 根据最新版本号跑一个新的容器即可

## 讨论群组
[🔗 t.me/jdx_discuss](https://t.me/jdx_discuss)
