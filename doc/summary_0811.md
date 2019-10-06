# LTL to PA 实现方案确定
## 经探索，epmc中LTL到PA的过程不便于复用
```
    1. 没有找到具体的测试用例来调试 LTL 到 PA的过程，通过阅读代码无法完全理解
    2. 为简化代码的理解，我们会根据empc中其他自动机的数据结构，重新设计一个PA的数据结构
        PA数据结构设计方案
            先做基本设计，之后会按需求完善其设计[UNDO]
```
## HOAParser 学习
```
    1. 进一步熟悉spot工具的使用
        根据算法需要: ltl2tgba 生成 parity="even" 的PA
        确定使用的命令: ltl2tgba -f "ltlstr" -peven -S
            -p colored PA
            -S 强制State-Base
    2. HOAParser熟悉及修改方案
        探索并理解HOAParser的解析逻辑
            目前HOAParser 可以解析出来Buchi
                解析得到GraphPreparator
                使用toGraph构造一个Buchi
            支持parity相关语法，但是不能解析得到PA, 而且存在一些语法描述的问题。
                acc-name: "parity" INT ("min" | "max") ("even" | "odd") INT
                这里第一个INT是多余的！！！
         修改方案[DOING]:
            修改HOAParser 解析的解析逻辑
                可能会略过一些检查(目前尚未理解)
            重写toGraph 方法构造我们自己的PA
     3. 遗留问题
        epmc 中调用 ltl2tgba 的参数设置问题
            有些参数设置，无输出
```
## 总结LTL到PA的实现方案
```
  1. 重新设计PA的数据结构，并修改HOAParser以得到我们想要的PA
  2. 制定PA 和 Model 的Product QMC-PA 的实现方案
```
## GetBSCC 算法实现
```
   1. GetBSCC 算法到python版的迁移已完成，并做了部分测试，目前没有遇到问题。 
        之后会做进一步的测试
   2. 第三步算法: PQMC值计算算法的实现方案制定中
   3. QMC-PA 与 第三步算法的交互方案的制定
```
