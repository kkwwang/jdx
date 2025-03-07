const fs = require('fs');
const sqlite3 = require('sqlite3').verbose();
const {
    getEnvs
} = require('./ql');


const fileNameReg = /(([0-9]{4}-[0-9]{2}-[0-9]{2})-([0-9]{2})-([0-9]{2})-([0-9]{2})-([0-9]{3})).log/

const logDir = '../../log/';


const contentReg = /\*\*\*\*\*\*\*\*\*开始查询【账号[0-9]*】.*\*\*\*\*\*\*\*\*\*\*\*/
const 账号Reg = /【账号[0-9]*🆔】(.*)\(.*\)/
const 账号信息Reg = /【账号信息】(.*)/
const 京豆变动Reg = /【京豆变动】([\-0-9.]*)豆/
const 京豆余额Reg = /【当前京豆】([0-9.]*)豆\(≈([0-9.]*)元\)/
const 老农场Reg = /【老农场】(.*)\(([0-9.]*)%\)/
const 新农场Reg = /【新农场】(.*)\(([0-9.]*)%\)/
const 话费积分Reg = /【话费积分】([0-9.]*)/
const 超市卡Reg = /【超市卡】([0-9.]*)元(\(([0-9.]*)元赠金[0-9.]*时[0-9.]*分后到期\))?/
const 玩一玩奖票Reg = /【玩一玩奖票】([0-9.]*)个/
const 汪贝余额Reg = /【汪贝余额】([0-9.]*)个/
const 省钱币Reg = /【省钱币】([0-9.]*)个/
const 红包总额Reg = /【红包总额】([0-9.]*)元(\(今日.过期([0-9.]*)\))?/
const 特价版APPReg = /【特价版APP】([0-9.]*)元(\(今日.过期([0-9.]*)\))?/
const 京东APPReg = /【京东APP】([0-9.]*)元(\(今日.过期([0-9.]*)\))?/
const 微信小程序Reg = /【微信小程序】([0-9.]*)元(\(今日.过期([0-9.]*)\))?/
const 全平台通用Reg = /【全平台通用】([0-9.]*)元(\(今日.过期([0-9.]*)\))?/


const db = new sqlite3.Database('../../db/bean-log.db', (err) => {
    if (err) {
        console.error(err.message);
    } else {
        console.log('Connected to the SQLite database.');
        readLogs()

    }
});

const initDb = () => {
    // 创建一个表
    db.run(`
        CREATE TABLE IF NOT EXISTS "main"."bean"
        (
            "序号"           integer,
            "mobile"         TEXT,
            "账号"           TEXT,
            "账号信息"       TEXT,
            "京豆变动"       integer,
            "京豆余额"       integer,
            "京豆余额元"     real,
            "老农场比例"     real,
            "新农场比例"     real,
            "话费积分"       real,
            "超市卡"         real,
            "超市卡过期"     real,
            "玩一玩奖票"     integer,
            "汪贝余额"       integer,
            "省钱币"         integer,
            "红包总额"       real,
            "红包过期"       real,
            "特价版APP"      real,
            "特价版APP过期"  real,
            "京东APP"        real,
            "京东APP过期"    real,
            "微信小程序"     real,
            "微信小程序过期" real,
            "全平台通用"     real,
            "全平台通用过期" real,
            "视频收益"       real,
            "视频余额"       real,
            "时间"           TEXT
        );
    `, (err) => {
        if (err) {
            console.error(err.message);
        } else {
            console.log('Table CREATE successfully.');
        }
    });


}


const readLogs = async () => {
    const envs = await getEnvs();
    const jdEnvs = {}
    envs.forEach(
        item => {
            const remarks = JSON.parse(item.remarks)
            jdEnvs[remarks.ptPin] = remarks
            jdEnvs[remarks.nickname] = remarks
        }
    )

    // 使用fs读取./log文件夹下的文件列表
    const dbData = []
    let id = 0
    const dates = new Set()

    fs.readdir(logDir, (err, dirs) => {
        dirs.forEach(dir => {
            if (dir.indexOf('bean_change') !== -1) {
                fs.readdir(logDir + dir, (err, files) => {
                    if (err) {
                        console.log(err);
                        return;
                    }

                    files.forEach(file => {
                        let fileNameMatch = file.match(fileNameReg);
                        // 使用正则匹配文件名，如果匹配成功，则删除文件
                        if (fileNameMatch && fileNameMatch.length >= 7) {
                            const content = fs.readFileSync(logDir + dir + "/" + file).toString()
                            const result = content.split(contentReg)
                            result.forEach(item => {

                                if (item.indexOf('【账号') !== -1) {
                                    const itemData = {}
                                    const lines = item.split('\n').filter(item => item.trim())
                                    lines.forEach(line => {
                                        const 账号Match = line.match(账号Reg)
                                        if (账号Match && 账号Match.length >= 2) {
                                            itemData.账号 = 账号Match[1]
                                            if (jdEnvs[itemData.账号]) {
                                                itemData.mobile = jdEnvs[itemData.账号].mobile
                                            }
                                        }
                                        const 账号信息Match = line.match(账号信息Reg)
                                        if (账号信息Match && 账号信息Match.length >= 2) {
                                            itemData.账号信息 = 账号信息Match[1]
                                        }

                                        const 京豆变动Match = line.match(京豆变动Reg)
                                        if (京豆变动Match && 京豆变动Match.length >= 2) {
                                            itemData.京豆变动 = parseInt(京豆变动Match[1])
                                        }
                                        const 京豆余额Match = line.match(京豆余额Reg)
                                        if (京豆余额Match && 京豆余额Match.length >= 3) {
                                            itemData.京豆余额 = parseInt(京豆余额Match[1])
                                            itemData.京豆余额元 = parseFloat(京豆余额Match[2])
                                        }
                                        if (itemData.京豆变动 === itemData.京豆余额 || itemData.京豆变动 < 0) {
                                            itemData.京豆变动 = null
                                        }
                                        //console.log(itemData.京豆变动)
                                        const 老农场Match = line.match(老农场Reg)
                                        if (老农场Match && 老农场Match.length >= 3) {
                                            //itemData.老农场 = 老农场Match[1]
                                            itemData.老农场比例 = parseFloat(老农场Match[2])
                                        }
                                        const 新农场Match = line.match(新农场Reg)
                                        if (新农场Match && 新农场Match.length >= 3) {
                                            //itemData.新农场 = 新农场Match[1]
                                            itemData.新农场比例 = parseFloat(新农场Match[2])
                                        }
                                        const 话费积分Match = line.match(话费积分Reg)
                                        if (话费积分Match && 话费积分Match.length >= 2) {
                                            itemData.话费积分 = parseFloat(话费积分Match[1])
                                        }
                                        const 超市卡Match = line.match(超市卡Reg)
                                        if (超市卡Match && 超市卡Match.length >= 4) {
                                            itemData.超市卡 = parseFloat(超市卡Match[1])
                                            itemData.超市卡过期 = parseFloat(超市卡Match[3] || 0) || null
                                        }
                                        const 玩一玩奖票Match = line.match(玩一玩奖票Reg)
                                        if (玩一玩奖票Match && 玩一玩奖票Match.length >= 2) {
                                            itemData.玩一玩奖票 = parseInt(玩一玩奖票Match[1])
                                        }
                                        const 汪贝余额Match = line.match(汪贝余额Reg)
                                        if (汪贝余额Match && 汪贝余额Match.length >= 2) {
                                            itemData.汪贝余额 = parseInt(汪贝余额Match[1])
                                        }
                                        const 省钱币Match = line.match(省钱币Reg)
                                        if (省钱币Match && 省钱币Match.length >= 2) {
                                            itemData.省钱币 = parseInt(省钱币Match[1])
                                        }
                                        const 红包总额Match = line.match(红包总额Reg)
                                        if (红包总额Match && 红包总额Match.length >= 4) {
                                            itemData.红包总额 = parseFloat(红包总额Match[1])
                                            itemData.红包过期 = parseFloat(红包总额Match[3] || 0) || null
                                        }
                                        const 特价版APPMatch = line.match(特价版APPReg)
                                        if (特价版APPMatch && 特价版APPMatch.length >= 4) {
                                            itemData.特价版APP = parseFloat(特价版APPMatch[1])
                                            itemData.特价版APP过期 = parseFloat(特价版APPMatch[3] || 0) || null
                                        }
                                        const 京东APPMatch = line.match(京东APPReg)
                                        if (京东APPMatch && 京东APPMatch.length >= 4) {
                                            itemData.京东APP = parseFloat(京东APPMatch[1])
                                            itemData.京东APP过期 = parseFloat(京东APPMatch[3] || 0) || null
                                        }
                                        const 微信小程序Match = line.match(微信小程序Reg)
                                        if (微信小程序Match && 微信小程序Match.length >= 4) {
                                            itemData.微信小程序 = parseFloat(微信小程序Match[1])
                                            itemData.微信小程序过期 = parseFloat(微信小程序Match[3] || 0) || null
                                        }
                                        const 全平台通用Match = line.match(全平台通用Reg)
                                        if (全平台通用Match && 全平台通用Match.length >= 4) {
                                            itemData.全平台通用 = parseFloat(全平台通用Match[1])
                                            itemData.全平台通用过期 = parseFloat(全平台通用Match[3] || 0) || null
                                        }
                                    })
                                    itemData.时间 = fileNameMatch[2]
                                    dates.add(itemData.时间)
                                    id++
                                    dbData.push([
                                        id,
                                        itemData.mobile,
                                        itemData.账号,
                                        itemData.账号信息,
                                        itemData.京豆变动,
                                        itemData.京豆余额,
                                        itemData.京豆余额元,
                                        itemData.老农场比例,
                                        itemData.新农场比例,
                                        itemData.超市卡,
                                        itemData.话费积分,

                                        itemData.超市卡过期,
                                        itemData.玩一玩奖票,
                                        itemData.汪贝余额,
                                        itemData.省钱币,
                                        itemData.红包总额,
                                        itemData.红包过期,
                                        itemData.特价版APP,
                                        itemData.特价版APP过期,
                                        itemData.京东APP,
                                        itemData.京东APP过期,
                                        itemData.微信小程序,
                                        itemData.微信小程序过期,
                                        itemData.全平台通用,
                                        itemData.全平台通用过期,
                                        itemData.时间
                                    ])
                                }
                            })
                        }
                    });
                    if (dbData.length) {
                        saveToDb(dbData, Array.from(dates))
                    }

                });

            }
        })
    })

}

const saveToDb = (dbData, dates) => {

    const sql = `INSERT INTO "main"."bean" ("序号",
                                            "mobile",
                                            "账号",
                                            "账号信息",
                                            "京豆变动",
                                            "京豆余额",
                                            "京豆余额元",
                                            "老农场比例",
                                            "新农场比例",
                                            "超市卡",
                                            "话费积分",
                                            "超市卡过期",
                                            "玩一玩奖票",
                                            "汪贝余额",
                                            "省钱币",
                                            "红包总额",
                                            "红包过期",
                                            "特价版APP",
                                            "特价版APP过期",
                                            "京东APP",
                                            "京东APP过期",
                                            "微信小程序",
                                            "微信小程序过期",
                                            "全平台通用",
                                            "全平台通用过期",
                                            "时间")
                 VALUES (?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?,
                         ?);`;

    db.serialize(() => {
        db.run("BEGIN TRANSACTION;");
        initDb()

        dbData.forEach(item => {
            // 删除已有数据
            db.run(`delete
                    from "main"."bean"
                    where 时间 = ?
                      and mobile = ?`, [item[item.length - 1], item[1]]);
            db.run(sql, item);
        })

        // 提交事务
        db.run("COMMIT;");

        db.close((err) => {
            if (err) {
                console.error(err.message);
            }
            console.log(`保存成功：${dbData.length}条`);
            console.log('数据库连接关闭。');
            console.log("日志读取完成")
        });
    });
}
