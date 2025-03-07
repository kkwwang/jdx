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
