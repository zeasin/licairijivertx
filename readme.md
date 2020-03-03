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
##### 2、写摘抄
功能类似写心得，不同之处是这里直接填外网url，并且会保存pdf。
##### 3、添加自选股



### 技术说明
#### vertx
采用vertx框架的异步web程序

#### pdf生成
利用wkhtmltopdf生成pdf，所以运行的本机需要安装wkhtmltopdf
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
