### 统计相关配置，脚本未加密混淆，请自行修改使用，欢迎提bug，但不对脚本负责


> after相关脚本配置到对应任务的after中   
> 保证bean-log优先于其他脚本之前运行

### after命令：

- task 6dylan6_jdpro_main/jd_bean_info.js
```bash
node ../其他/bean-log.js && node ../其他/after_bean_info.js
```

- task 6dylan6_jdpro_main/jd_bean_change.js
```bash
node ../其他/bean-log.js &&  node ../其他/after-video.js
```

- task 6dylan6_jdpro_main/jd_CheckCK.js
```bash
node ../其他/after_check_ck.js
```

- task 6dylan6_jdpro_main/jd_video_view.js
- task 6dylan6_jdpro_main/jd_video_task.js
```bash
node ../其他/bean-log.js &&  node ../其他/after-video.js
```


- 手动执行
```txt
名称：刷新统计
命令/脚本：task 其他/bean-log.js
定时规则：0 0 * * *
执行后：node ../其他/after-video.js && node ../其他/after_bean_info.js
```

### patch文件
patch文件仅对`6dylan6_jdpro`的sendNotify.js进行修改，若`git apply`失败，请联系作者重新获取

修改订阅任务执行后
```bash
/bin/bash /ql/data/scripts/其他/after-subscription.sh
```

### 相关环境变量：
- BEAN_ALLNOTIFY
```txt
资产统计：
https://域名:端口/bean/
```
> 京东资产统计
> 
> 
> 【账号17🆔】************(已实名)   
> 【账号信息】铜牌会员   
> 【京豆变动】3豆(与昨日21:00比较)   
> 【当前京豆】618豆(≈6.18元)   
> 【新农场】种植进度4/5(31.21%)   
> 【话费积分】7.26   
> 【玩一玩奖票】3161个   
> 🧧🧧🧧红包明细🧧🧧🧧   
> 【红包总额】0.53元(今日总过期0.30)   
> 【特价版APP】0.34元(今日将过期0.30)   
> 【全平台通用】0.19元   
> 
> 资产统计：   
> https://域名:端口/bean/************/bean


- CHECKCK_ALLNOTIFY
```txt
每月到客户服务-在线客服发'火爆了'，如出滑块就拼一下会解除一些活动火爆

为了连续任务完整性，请尽量在15:00前登录。
登录地址： https://域名:端口/
```

> CK检测
>    
> 京东CK检测：   
>    
> 👇👇👇👇失效账号👇👇👇👇   
> ************   
>    
>    
> 【✨✨✨✨温馨提示✨✨✨✨】   
> 每月到客户服务-在线客服发'火爆了'，如出滑块就拼一下会解除一些活动火爆   
>    
>    
> 为了连续任务完整性，请尽量在15:00前登录。   
> 登录地址： https://域名:端口/************   

- qywxyy
```txt
企业微信应用配置 企业id,应用id,应用secret
```
> 企业微信应用配置，用于一对一推送

- QYWXYYY_ADMIN_USER
```txt
@all
```
> 企业微信管理员通知应用。请自行控制应用可见范围。企业微信相关配置自配

- qywxyy_admin
```txt
企业微信管理员通知应用配置 企业id,应用id,应用secret
```
> 
