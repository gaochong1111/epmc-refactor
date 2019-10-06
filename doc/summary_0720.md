# 基于epmc的量子模型检测工具开发第一阶段工作总结
## 已完成工作
### 搭建开发环境，了解empc工具的运行逻辑
- 在ubuntu系统和mac系统分别搭建开发环境    
    ```
    发现目前qmc的测试用例无法在eclipse开发环境中直接运行的问题，
    上报并最终由Moritz修复。
    ```
- 依据qmc-loop的测试用例，了解工具的运行逻辑
    ```
    使用代码阅读和debug相结合的方式了解epmc工具的运行逻辑。
    整理运行逻辑如下：
    1. 解析命令行参数，进行plugin的加载, 同时初始化全局的Option上下文对象
        Q: Option对象里存储的配置项过多，生命周期太长，难于理解。
    2. 根据Option选择合适的解析器对模型文件进行解析得到模型内部表示 [解析模块]
        目前模型解析器包括：PrismParser 和 QMCParser
        模型内部表示包括：ModelPRISM 和 ModelPRISMQMC
        Q: ModelPRISMQMC类未在工具中使用？
    3. 根据Option选择合适的解析器对属性文件进行解析得到属性公式的内部表示[解析模块]
        目前属性解析器包括：PrismExpressionParser 和 QMCExpressionParser
        属性解析过程在Property相关对象的方法中
        Property的实现类: PropertyPRISM 和 PropertyPRISMQMC
        Q: 非qctl公式,比如pctl, ltl等公式解析后是怎么区分的？目前还不清楚。
    4. Model Checking: 解析得到的模型和属性公式 [核心求解模块]
        在Option中的solver列表中选择合适的plugin进行求解
        选择判定逻辑在solver的canHandle方法中
        求解算法实现在solver的solver方法中
    5. 报告求解结果
    ```
### 解析模块相关工作
- 整理epmc中qmc模型描述语言和qctl属性描述语言的语法文档
[参考链接](https://github.com/gaochong1111/epmc/blob/master/doc/syntax.md)
- 制定语法修改方案并实现
    ```
    1. 修改方案
        语法修改依据之前同冯元老师讨论的结果，
        涉及qmc中vector，matrix的定义形式的修改等,
        具体修改方案参考语法文档。
    2. 实现方案
        根据修改后的语法文档，修改对应的语法描述文件*.jj
        对已有的三个测试用例修改后分别进行测试通过
    ```
### 求解模块相关工作
- 冯元老师整理了qmc+ltl相关算法和示例文档[参考链接](https://github.com/gaochong1111/dev-doc/blob/master/LTL.pdf)
    ```
    对算法描述文档进行初步理解，将算法分三步进行实现
        1. ltl公式到lowlevel表示PA的转换的实现
        2. QMC模型表示和PA的Product的实现
        3. 在QMC-PA Product 中(s,a)上计算一个值
    ```
- 在epmc中新建了求解plugin
    ```
    参考propertysolver-ltl-lazy新加了propertysolver-ltl-qmc求解插件，并测试运行完成模型解析和属性解析
    Q1: ltl公式解析部分测试成功
        需了解目前epmc所支持的ltl公式是怎么处理的，考虑复用。

    ```
## 当前进行工作 
### 正在制定qmc+ltl model checking 实现方案
- 一、ltl公式到PA转换的实现方案
    ```
        方案一：复用epmc中PA的设计方案
        1. 探索epmc中对于PA的数据结构定义 
            automata.determinisation.AutomatonScheweParity
            以此为线索，进行代码阅读和UML辅助工具理解PA的设计思路。
            由于相关类比较多，暂时没有理解透彻：
                涉及有Automata相关、Buechi相关、Graph相关、HoaParser等实现类
        2. 探索epmc中对于PA的使用 
            探索思路：根据epmc中支持的ltl相关模型检测测试用例,通过debug的方法理解ltl到自动机的构造过程
            2.1 ltl 到 Buechi 的转换过程是通过调用SPOT的命令并通过HoaParser解析完成的
            Q1: 目前尚未定位到epmc中ltl到PA的转换过程
            Q2: 发现epmc对于ltl相关的测试用例进行求解的BUG，已上报Moritz正在修复
            2.2 通过现有测试用例了解epmc中ltl到PA转换过程 [TODO]
            Q3: 需要有人给出这样一个例子以语义解释，以便后续调试工作的进展。
        3. 目前以整理出SPOT命令输出的HOA格式的语法文件
            参考下面链接
            对于SPOT的工具使用正在熟悉 [DOING]
        方案二：重新设计相关数据结构的设计方案
            可以等对epmc中PA了解后讨论是否符合我们算法的需求。
            我们明确需求后可以重新设计一套自己的数据结构用于实现算法。
    ```
    [HOA语法参考链接](https://github.com/gaochong1111/epmc/blob/master/doc/hoa_syntax.md)
### propertysolver-ltl-qmc 插件实现方案
     1. 完成qmc模型解析和ltl公式解析
        qmc模型解析输出已完成
        ltl公式解析未完成 [DOING]
            需了解确认epmc中对ltl公式的定义形式
     2. canHandle方法的实现 [TODO]
     3. doSolve方法的实现 [TODO]
## 下一步工作计划
### 一、继续当前工作，尽快确认ltl到PA的实现方案
### 二、制定QMC-PA实现方案
### 三、对求值算法进一步理解后制定实现方案

## 工作感悟
### 影响工作进度的原因分析
- 对于epmc工具的理解进程比较慢
- 理论背景知识的欠缺
 
