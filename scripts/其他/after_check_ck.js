const fs = require('fs');
const sqlite3 = require('sqlite3').verbose();
const {
    getEnvs
} = require('./ql');

const fileNameReg = /(([0-9]{4}-[0-9]{2}-[0-9]{2})-([0-9]{2})-([0-9]{2})-([0-9]{2})-([0-9]{3})).log/

const reg = /京东账号[0-9]* : (.*) 已失效,自动禁用成功!/
const reg2 = /京东账号(.*)已失效,自动禁用成功!/
const ptPins = []
const envObj = {}

const readLog = async () => {

    const envs = await getEnvs();

    envs.forEach(env => {
        const remarks = JSON.parse(env.remarks)
        envObj[remarks.ptPin] = remarks
    })

    const dirs = fs.readdirSync('../../log')
    dirs.forEach(dir => {
        if (fs.statSync('../../log/' + dir).isDirectory()) {
            const files = fs.readdirSync('../../log/' + dir)
            files?.forEach(file => {
                let fileNameMatch = file.match(fileNameReg);
                if (fileNameMatch != null) {

                    const day = fileNameMatch[2]
                    const hour = fileNameMatch[3]
                    const minute = fileNameMatch[4]

                    const content = fs.readFileSync('../../log/' + dir + "/" + file).toString()
                    const lines = content.split('\n').filter(item => item.trim())

                    lines.forEach(line => {
                        const match = line.match(reg) || line.match(reg2)
                        if (match && match.length >= 2) {
                            const ptPin = match[1]
                            if (envObj[ptPin]) {
                                const mobile = envObj[ptPin].mobile
                                ptPins.push(
                                    [mobile, day, `${hour}:${minute}`]
                                )
                            }
                        }
                    })
                }
            })
        }


    })

    console.log("读取完成")

    db.serialize(() => {
        db.run("BEGIN TRANSACTION;");
        let lengthCount = 0;

        for (let index in ptPins) {
            // 删除已有数据
            db.run(`delete
                    from "main"."login_log"
                    where login_day = ?
                      and mobile = ?
                      and type = '0'`, [ptPins[index][1], ptPins[index][0]]);

            const sqlStr = `INSERT INTO "login_log" ("mobile", "login_day", "login_time", "type")
                            VALUES (?, ?, ?, 0);`

            db.run(sqlStr, ptPins[index]);
            lengthCount++
        }


        // 提交事务
        db.run("COMMIT;");

        console.log(`已保存条数：${lengthCount}条`)

        db.close((err) => {
            if (err) {
                console.error(err.message);
            }
            console.log('数据库连接关闭。');
            console.log("日志读取完成")

        });

    })
}

const db = new sqlite3.Database('../../db/bean-log.db', (err) => {
    if (err) {
        console.error(err.message);
    } else {
        console.log('Connected to the SQLite database.');
        readLog()
    }
});