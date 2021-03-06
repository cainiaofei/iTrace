
# iTrace
## iTrace能做什么？
给定项目的需求和代码，iTrace可以辅助生成代码和需求之间的对应关系。
输出样例：

|class|req|similarity|
|-----|---|----------|
|A|uc15|0.8912|
|B|uc15|0.6792|
|...|
|F|uc15|0.00000001|

similarity越大表示代码(class)和需求(req)之间具备相关性的可能越大。
## iTrace是怎么实现的？
架构图如下：
![image](https://github.com/cainiaofei/iTrace/raw/master/image/iTrace结构图.png?raw=true)
如图所示，首先，一方面基于开源工具jsoup爬取数据；另一方面，运行项目(java项目)并将基于jvmti实现的代码依赖捕获工具插桩到虚拟机中，获取代码依赖数据。然后，对数据进行分词，去停用词，词根还原等文本预处理，这里的数据集是oracle，也是接下来试验方法数据的输入。接下来，基于软件可追踪生成方法建立需求和代码的关联关系。最后，检验方法效果并展示pr图。
代码依赖捕获工具结构图如下：
![](https://github.com/cainiaofei/iTrace/raw/master/image/codeDependency.png)
