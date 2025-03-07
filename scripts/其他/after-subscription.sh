rm /ql/data/repo/6dylan6_jdpro_main/*.patch
cp /ql/data/scripts/其他/*.patch /ql/data/repo/6dylan6_jdpro_main
cd /ql/data/repo/6dylan6_jdpro_main
git apply `ls -1 *.patch`
cp /ql/data/repo/6dylan6_jdpro_main/sendNotify.js /ql/data/scripts/6dylan6_jdpro_main
cp /ql/data/repo/6dylan6_jdpro_main/sendNotify.js /ql/data/scripts/其他
cp /ql/data/repo/6dylan6_jdpro_main/sendNotify.js /ql/data/scripts

echo "查找关键字'好物'，正确为无结果"
cat /ql/data/scripts/6dylan6_jdpro_main/sendNotify.js | grep 好物

echo "查找关键字'BEAN_ALLNOTIFY'，正确为有结果"
cat /ql/data/scripts/6dylan6_jdpro_main/sendNotify.js | grep BEAN_ALLNOTIFY

echo "查找关键字'sendToQywxYy'，正确为有结果"
cat /ql/data/scripts/6dylan6_jdpro_main/sendNotify.js | grep sendToQywxYy

rm /ql/data/repo/6dylan6_jdpro_main/*.patch

echo "结束"