 

应用开发环境所需安装的工具包括JDK、Maven和Eclipse。如果已经安装了这些开发工具，请跳过本节。

## JDK约束及安装步骤

1.JDK版本

JDK版本要求1.8以上。

2.JDK下载

请到JDK 1.8版本的官方地址下载。

3.JDK安装

在官网下载JDK安装包后，选择合适的安装路径安装JDK。

这里以windows系统为例：

设置JAVA\_HOME环境变量，指向Java安装目录。将%JAVA\_HOME%\bin添加到系统路径path中。环境变量配置完成后，使用java -version命令验证是否安装成功，windows环境下回显如下：

```
C:\Users\h00407576> java -version      
 java version "1.8.0_121"      
 Java(TM) SE Runtime Environment (build 1.8.0_121-b13)      
 Java HotSpot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)
```

## Maven安装步骤

Maven是一款集项目管理、代码编译、工程管理等等能力于一体的开发工具。

### **前提条件**

JDK已经安装成功。

### **安装步骤**

a.在官方地址下载Maven安装包。

b.（可选）在官方地址下载Eclipse插件m2eclipse。最新的Eclipse版本中带有Maven插件，则不用下载此插件

c.解压Maven安装包到本机路径。

d.设置环境变量。

* 设置M2\_HOME环境变量，指向Maven安装目录。

* 将%M2\_HOME%\bin添加到系统路径path中。

e.（可选）设置本地仓库路径，用于保存从远程仓库获取的插件和依赖的副本。

这里以路径D:\maven\repository为例。在/conf中找到settings.xml文件，设置localRepository为D:\maven\repository

f.结果验证

使用mvn -version命令验证是否安装成功，windows环境下回显如下：

```
C:\Users\h00407576>mvn -version        
 Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-11T00:41:4        
 7+08:00)        
 Maven home: D:\soft\apache-maven-3.3.9-bin\apache-maven-3.3.9        
 Java version: 1.8.0_121, vendor: Oracle Corporation        
 Java home: C:\Program Files\Java\jdk1.8.0_121\jre        
 Default locale: zh_CN, platform encoding: GBK        
 OS name: "windows 7", version: "6.1", arch: "amd64", family: "dos"
```

## Eclipse安装

### **前提条件**

a.JDK已经安装。

b.Maven已经安装。

### **安装步骤**

a.在官方地址下载Eclipse安装包。

b.安装Eclipse到本机。

c.（可选）将之前Maven安装中介绍的插件m2eclipse解压到Eclipse安装目录下的plugins和features目录。最新的Eclipse版本

中带有Maven插件，不要进行此操作

d.启动Eclipse，配置jre、maven settings以及默认编码格式为utf-8。

