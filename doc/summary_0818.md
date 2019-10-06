## QMC-PA 实现方案
### 探索epmc中已实现的Product实现方案
```
在测试框架下，调试探索：
    ctmc模型对应的Graph与LTL属性公式对应Graph做Product的示例
    实现类:
        ProductGraphExplicit
    Product 结果表示: 
        GraphExplicitWrapper
    之后求解使用的是 GraphExplicitWrapper 的实例
    理解算法的关键在于:[DOING]
        ProductGraphExplicit 到 GraphExplicitWrapper 的转换
        以及 GraphExplicitWrapper 实例的使用过程
下一步计划:
    在了解以上过程的基础上，参考实现 QMC 与 LTL对应Graph的Product
        实现一个新的ProductGraphExplicit类
            具体需要实现的函数需要进一步探索
    
```

## PQMC 值计算算法实现方案
```
    基于GetBSCC的实现，探索第三步整体算法的实现
        1. 确定算法的输入描述 [TODO]
            QMC-PA 与 第三步算法的交互方式的确定
            待Porudct方案成形之后讨论确定
        2. 探索除GetBSCC算法之外的过程的实现 【DOING】
            关键在于对线性代数库的依赖，
            需要确定目前的线性代数库是否支持所有的操作
```
