# 项目阶段总结与规划
```
    明确项目目标: 最迟11月完成examples的测试,可以进行项目演示
```
## 第一阶段工作总结
```
    目前项目进度: 60%
        算法实现子目标:
            1. 从LTL公式构造PA完成度90%
                后期需要改进PA的设计
            2. 构造PA与QMC的乘积PQMC完成度50%
                实现思路已明确
                实现方案正在讨论确定中
            3. 算法2计算PQMC的值M 完成度70%
                核心算法GetBSCCs已实现
                算法2实现方案已确定
                代码实现已完成90%

        (注: 算法是指冯元老师提供文档中提到的算法)
```
# 第二阶段工作计划
```
    1. 9月-10月中旬 完成两个模块的代码实现,并制定联调方案
        目标: 一个example的功能测试
    2. 10月-11月 根据测试结果, 完善实现方案
        迭代: 测试->修改实现->测试...
        目标: 完成多个examples的演示
```
# 本周工作进展
## 子目标2: 构造PA与QMC的乘积PQMC 完成度50%
### epmc项目内构建新的求解插件 qltl-solver
```
    1. 完成qmc->graph实现过程的迁移 100%
    2. 完成ltl->PA实现过程的迁移 100%
    3. 制定Product实现方案 30%
        PQMC定义已基本理解
        阻塞问题:
            对于epmc中数据结果的理解不完全
                比如, 图中 节点属性,与边上的属性存储形式需要明确
```
## 子目标3: 算法2计算PQMC的值M 完成度70%
```
    1. 正在实现算法2的代码 90%
```
