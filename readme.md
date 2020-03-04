# 功能说明
### 主体功能
该系统主要围绕自选股功能展开，功能包括：
+ 自选股（资讯、公告、研报）
+ 交易记录
+ 收集相关资讯生成pdf
### 功能描述
##### 1、首页
```
首页展示用户的所有动态，包括写心得、添加自选股、股票交易等
```
##### 2、写心得
用来写交易心得的，可以添加三种标签：
+ Tags（用T进行搜索，只能加一个，保存最后一个）
+ 关联的板块Plate（用P进行搜索，可以加多个）
+ 关联的股票代码（用S进行搜索，可以加多个）
##### 3、写摘抄
功能类似写心得，不同之处是这里直接填外网url，并且会保存pdf。
##### 4、添加自选股

##### 5、我的自选股
+ 所有自选股股价变动信息，采用选股报接口
```
https://flash-api.xuangubao.cn/api/stock/data?symbols=000300.SS,399001.SZ,399006.SZ,000582.SZ,000905.SZ,600279.SS,600018.SS,601018.SS,000507.SZ,600190.SS,600017.SS,600317.SS,601000.SS,000088.SZ,601880.SS,601008.SS,002040.SZ,600717.SS,601228.SS,601298.SS,001872.SZ,601326.SS&fields=symbol,stock_chi_name,change_percent,price,turnover_ratio,non_restricted_capital,total_capital,per,limit_up_days,last_limit_up,limit_status,nearly_new_acc_pcp&strict=true
```
+ 获取自选股labels信息
```
https://flash-api.xuangubao.cn/api/stock_label/labels?symbols=000582.SZ,000905.SZ,600279.SS,600018.SS,601018.SS,000507.SZ,600190.SS,600017.SS,600317.SS,601000.SS,000088.SZ,601880.SS,601008.SS,002040.SZ,601326.SS,601298.SS,601228.SS,600717.SS,001872.SZ
```
+ 获取自选股资金流向
```
https://api-ddc-wscn.xuangubao.cn/market/fundflow/batch?prod_codes=300292.SZ,300339.SZ,603322.SS,300248.SZ,300356.SZ,300466.SZ,688080.SS,600718.SS,600804.SS,002089.SZ,002757.SZ,300213.SZ,300249.SZ,300050.SZ,300166.SZ,300353.SZ,603083.SS,300660.SZ,600845.SS,300052.SZ,300287.SZ,300307.SZ,300448.SZ,300310.SZ,300504.SZ,600589.SS,002121.SZ,002261.SZ,002559.SZ,300017.SZ,300687.SZ,603803.SS,002049.SZ,002313.SZ,300232.SZ,300370.SZ,300730.SZ,000530.SZ,002528.SZ,300384.SZ,300638.SZ,000711.SZ,000851.SZ,002152.SZ,300036.SZ,600064.SS,002439.SZ,300183.SZ,300552.SZ,300682.SZ,000977.SZ,002421.SZ,300113.SZ,300597.SZ,000034.SZ,000793.SZ,600756.SS,300175.SZ,002169.SZ,002642.SZ,002929.SZ,300020.SZ,300645.SZ,000063.SZ,002184.SZ,002335.SZ,300157.SZ,000971.SZ,300212.SZ,300496.SZ,300738.SZ,002123.SZ,300360.SZ,300514.SZ,300603.SZ&day_count=5
```
##### 6、股票详情
+ 获取股票基本信息
+ 获取股票相关资讯

### 技术说明
#### vertx
采用vertx框架的异步web程序

#### pdf生成
利用wkhtmltopdf生成pdf，所以运行的本机需要安装wkhtmltopdf
为什么要生产pdf，主要是因为怕网上的资源无法访问了，特别是微信公众号的文章访问url是动态的。
##### wkhtmltopdf环境安装
+ Windows安装
```
下载exe安装包，安装
配置path
C:\Program Files\wkhtmltopdf\bin
```
+ mac安装
#### 股票数据
主要利用xuangubao.cn接口获取最新股票数据

#### jsoup
jsoup 是一款 Java 的 HTML 解析器，可直接解析某个 URL 地址、HTML 文本内容。它提供了一套非常省力的 API，可通过 DOM，CSS 以及类似于 jQuery 的操作方法来取出和操作数据。
jsoup 的主要功能如下：[使用参考](https://www.ibm.com/developerworks/cn/java/j-lo-jsouphtml/)
[使用文档](https://www.open-open.com/jsoup/)

+ 1. 从一个 URL，文件或字符串中解析 HTML；
+ 2. 使用 DOM 或 CSS 选择器来查找、取出数据；

+ 3. 可操作 HTML 元素、属性、文本；


#### 文件存储在七牛云
##### 七牛云备份方法
[参考0](https://www.laozuo.org/11195.html)
[参考1](https://github.com/qiniu/qshell/blob/master/docs/qdownload.md)
[参考2](https://developer.qiniu.com/kodo/tools/1302/qshell)
+ qshell全局配置
```
sudo vim $HOME/.qshell.json

{
    "hosts": {
        "rs": "rs.qiniu.com"
    }
}
```
+ 批量下载备份配置文件
```
sudo vim qdisk_down.conf


{
    "dest_dir"   :   "/Users/zhuming/Downloads/qshell-v2.3.6/down",
    "bucket"     :   "licairiji",
    "prefix"     :   "",
    "suffixes"   :   "",
    "cdn_domain" :   "licai.xiguanapp.com",
    "referer"    :   "http://licai.xiguanapp.com",
    "log_file"   :   "download.log",
    "log_level"  :   "info",
    "log_rotate" :   1,
    "log_stdout" :   false
}

```
+ 批量下载命令
```
cd /Users/zhuming/Downloads/qshell-v2.3.6

./qshell qdownload  qdisk_down.conf 
```

### 数据来源
+ [投研数据](https://robo.datayes.com/v2/fastreport/company?subType=%E4%B8%8D%E9%99%90&induName=)
