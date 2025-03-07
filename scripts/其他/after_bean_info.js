const fs = require('fs');
const sqlite3 = require('sqlite3').verbose();

const db = new sqlite3.Database('../../db/bean-log.db', (err) => {
    if (err) {
        console.error(err.message);
    } else {
        console.log('Connected to the SQLite database.');
        readLogs()
    }
});

const fileNameReg = /(([0-9]{4}-[0-9]{2}-[0-9]{2})-([0-9]{2})-([0-9]{2})-([0-9]{2})-([0-9]{3})).log/
const ptPinReg = /[0-9]*：(.*) 京豆详情统计】/
const beanReg = /今日收入总计：([0-9]*)京豆 🐶/
const dataObj = {}

const readLogs = async () => {

    const logDir = '../../log/';
    const jd_bean_info_dir = 'jd_bean_info'

    const logDirs = fs.readdirSync(logDir)
    logDirs.forEach(dir => {
        if (dir.indexOf(jd_bean_info_dir) !== -1) {
            const logFiles = fs.readdirSync(logDir + dir)
            logFiles.forEach(file => {
                let fileNameMatch = file.match(fileNameReg);
                const day = fileNameMatch[2]
                if (!dataObj[day]) {
                    dataObj[day] = {}
                }
                const content = fs.readFileSync(logDir + dir + "/" + file).toString()
                const result = content.split('\n\n【账号').filter(item => item.trim() && item.indexOf("京豆详情统计】") !== -1)

                result.forEach(item => {
                    const lines = item.split('\n').filter(item => item.trim())
                    let ptPin = null
                    lines.forEach(line => {
                        const 账号Match = line.match(ptPinReg)
                        if (账号Match && 账号Match.length >= 2) {
                            ptPin = 账号Match[1]
                            if (!dataObj[day][ptPin]) {
                                dataObj[day][ptPin] = []
                            }
                        }
                        const beanMatch = line.match(beanReg)
                        if (beanMatch && beanMatch.length >= 2) {
                            dataObj[day][ptPin].push(parseInt(beanMatch[1]))
                        }
                    })
                })
            })
        }
    })

    console.log("读取完成")

    db.serialize(() => {
        db.run("BEGIN TRANSACTION;");
        Object.keys(dataObj).forEach(day => {
            const ptPins = dataObj[day]
            Object.keys(ptPins).forEach(ptPin => {
                const beans = ptPins[ptPin]
                if(beans.length > 0){
                    const bean = Math.max(...beans)
                    const sql = `UPDATE "main".bean
                             SET "京豆变动" = ${bean}
                             WHERE "账号" = '${ptPin}'
                               AND "时间" = '${day}'
                               AND ("京豆变动" IS NULL OR "京豆变动" < ${bean})`
                    // console.log(sql)
                    db.run(sql);
                }

            })
        })

        // 提交事务
        db.run("COMMIT;");

        db.close((err) => {
            if (err) {
                console.error(err.message);
            }
            console.log('数据库连接关闭。');
            console.log("日志读取完成")

        });
    })

}

