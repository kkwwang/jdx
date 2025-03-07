const fs = require('fs');
const dayjs = require('dayjs');

const sqlite3 = require('sqlite3').verbose();

const all = true
const logRootPath = '../../log/'

const {
    getEnvs
} = require('./ql');


const db = new sqlite3.Database('../../db/bean-log.db', (err) => {
    if (err) {
        console.error(err.message);
    } else {
        console.log('Connected to the SQLite database.');
        readLogs()
    }
});

const fileNameReg = /(([0-9]{4}-[0-9]{2}-[0-9]{2})-([0-9]{2})-([0-9]{2})-([0-9]{2})-([0-9]{3})).log/
const data = {}
let envs = [];
let 风控Set = new Set()

const readLogs = async () => {
    envs = await getEnvs();
    readTaskLogs()

    readVideoViewLogs()
    afterAll()
}

const readVideoViewLogs = () => {
    const 现金合计Reg = /账号([0-9]*)【(.*)】现金总计：([0-9.]*)/
    const 提现Reg = /账号([0-9]*)【(.*)】提现到余额成功 ([0-9.]*)/
    const 风控Reg = /账号([0-9]*)【(.*)】风控用户，无法获得金币！/

    风控Set = new Set()
    const logDirs = fs.readdirSync(logRootPath)
    logDirs.forEach(logDir => {
        if (logDir.indexOf('video_view') !== -1) {
            const video_view_files = fs.readdirSync(logRootPath + logDir)

            for (let file of video_view_files) {

                let fileNameMatch = file.match(fileNameReg);
                const day = fileNameMatch[2]


                if (!data[day]) {
                    data[day] = {
                        现金合计Obj: {},
                        提现记录Obj: {}
                    }
                }


                const content = fs.readFileSync(logRootPath + logDir + "/" + file).toString()
                const lines = content.split('\n').filter(item => item.trim())

                const 提现Obj = {}
                const 现金总计Obj = {}

                lines.forEach(line => {
                    const 提现Match = line.match(提现Reg)
                    if (提现Match && 提现Match.length >= 4) {
                        const env = 提现Match[2]

                        if (!data[day].提现记录Obj[env]) {
                            data[day].提现记录Obj[env] = 0
                        }
                        const 提现 = parseFloat(提现Match[3])
                        data[day].提现记录Obj[env] += 提现
                        提现Obj[env] = 提现
                    }

                    const 现金合计Match = line.match(现金合计Reg)
                    if (现金合计Match && 现金合计Match.length >= 4) {
                        const env = 现金合计Match[2]

                        const 现金合计 = parseFloat(现金合计Match[3])
                        现金总计Obj[env] = 现金合计
                    }

                    if (day == dayjs().format('YYYY-MM-DD')) {
                        const 风控Match = line.match(风控Reg)
                        if (风控Match && 风控Match.length >= 2) {
                            const env = 风控Match[2]
                            风控Set.add(env)
                        }
                    }
                })
                // 计算剩余现金
                for (let env in 现金总计Obj) {
                    const 现金总计 = 现金总计Obj[env] - (提现Obj[env] || 0)

                    if (现金总计) {
                        data[day].现金合计Obj[env] = 现金总计
                    }
                }

            }
        }
    })

}

const readTaskLogs = () => {
    const contentReg = /开始【/
    const 现金总计Reg = /现金总计：([0-9.]*)/
    const 账号Reg = /账号[0-9]*】(.*)((\*\*\*\*\*\*\*\*\*)|(--------))/


    const logDirs = fs.readdirSync(logRootPath)
    logDirs.forEach(logDir => {

        if (logDir.indexOf('video_task') !== -1) {
            const video_task_files = fs.readdirSync(logRootPath + logDir)
            for (let file of video_task_files) {
                let fileNameMatch = file.match(fileNameReg);
                const day = fileNameMatch[2]
                if (!data[day]) {
                    data[day] = {
                        现金合计Obj: {},
                        提现记录Obj: {}
                    }
                }

                const content = fs.readFileSync(logRootPath + logDir + "/" + file).toString()

                const result = content.split(contentReg)
                result.forEach(item => {

                    const 账号Match = item.match(账号Reg)
                    if (账号Match && 账号Match.length >= 2 && 账号Match[1]) {
                        let env = 账号Match[1]

                        let 提现 = 0
                        let 现金总计 = 0
                        const lines = item.split('\n').filter(item => item.trim())
                        lines.forEach(line => {

                            if (line.indexOf("元可在提现页面查看打款记录") !== -1) {
                                const 签到提现 = parseFloat(line.replace("签到成功：余额到账", "").replace("元可在提现页面查看打款记录", ""))
                                data[day].提现记录Obj[env] = (data[day].提现记录Obj[env] || 0) + 签到提现
                            }

                            if (line.indexOf("提现到钱包成功") !== -1) {
                                提现 = parseFloat(line.replace("提现到钱包成功 ", "").replace("元 💰️", ""))
                                data[day].提现记录Obj[env] = (data[day].提现记录Obj[env] || 0) + 提现
                            }


                            const 现金总计Match = line.match(现金总计Reg)
                            if (现金总计Match && 现金总计Match.length >= 2) {
                                现金总计 = parseFloat(现金总计Match[1])
                            }
                        })
                        // 计算剩余现金
                        现金总计 = parseFloat((现金总计 - 提现).toFixed(2))

                        if (现金总计) {
                            data[day].现金合计Obj[env] = 现金总计
                        }
                    }
                })
            }
        }

    })
}

const afterAll = () => {
    console.log("读取完成")

    db.serialize(() => {
        db.run("BEGIN TRANSACTION;");
        const count = [];
        let lengthCount = 0;

        const videoBlacklist = []

        envs.forEach(env => {
            const remarks = JSON.parse(env.remarks)
            const ptPin = remarks.ptPin


            const item = {
                账号: remarks.wechat,
                金额: 0,
                余额: 0
            }

            for (let day in data) {
                const 现金合计Obj = data[day].现金合计Obj
                const 提现记录Obj = data[day].提现记录Obj

                item.金额 = parseFloat((item.金额 + (提现记录Obj[ptPin] || 0)).toFixed(2))
                item[day] = parseFloat((提现记录Obj[ptPin] || 0).toFixed(2))

                item.余额 = parseFloat((现金合计Obj[ptPin] || (item.余额 || 0)).toFixed(2))

                const rowData = [
                    item.余额,
                    parseFloat((提现记录Obj[ptPin] || 0).toFixed(2)),
                    remarks.mobile,
                    day
                ]

                if (day == dayjs().format('YYYY-MM-DD') || all) {

                    db.run(`UPDATE "main".bean
                            SET "视频余额" = ?,
                                "视频收益" = ?
                            WHERE "mobile" = ?
                              AND "时间" = ?`, rowData);
                    lengthCount++
                }


            }

            if (风控Set.has(ptPin)) {
                videoBlacklist.push(remarks)
            }

            item.金额 = parseFloat(item.金额.toFixed(2))

            count.push(item)


        });

        fs.writeFileSync('videoBlacklist.json', JSON.stringify(videoBlacklist, null, '\t'))

        let 现金合计 = 0
        let 提现记录 = 0

        count.forEach(item => {
            现金合计 += item.余额
            提现记录 += item.金额
        })

        console.log("未提取总金额：" + parseFloat(现金合计.toFixed(2)))
        console.log("已提取总金额：" + parseFloat(提现记录.toFixed(2)))


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

