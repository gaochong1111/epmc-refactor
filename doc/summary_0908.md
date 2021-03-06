# 项目阶段总结与规划

```
    明确项目目标: 最迟11月完成examples的测试,可以进行项目演示

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

## 子目标2: 构造PA与QMC的乘积PQMC 完成度 (50%->70%)

### epmc项目内构建新的求解插件 qltl-solver

```
    1. 完成qmc->graph实现过程的迁移 100%
    2. 完成ltl->PA实现过程的迁移 100%
    3. 制定Product实现方案 20%->50%
        PQMC定义已基本理解 100%
        阻塞问题:
            对于epmc中数据结果的理解不完全
        解决方案：50%
            1. 先根据算法2的需求导出指定格式的输出 100%
            	格式已讨论确定：使用目前算法2测试用的输入格式
           	2. 初步Product的实现方案已确定，代码实现中 40%
        	3. 评估是否需要构造图表示[DOING] 0%
            
```

## 子目标3: 算法2计算PQMC的值M 完成度 （70%->75%)

```
    1. 正在实现算法2的代码 90% -> 100%
    2. 正在测试算法2的实现代码 10%
        Q：目前的测试过程中发现算法2实现中的性能瓶颈
        	下图中红色框里， 关于矩阵M的Jordan分解， 我们使用的是Python的sympy库
        	当M规模比较小时，Jordan分解过程会有卡顿现象，当规模比较大时得不到结果。
        	目前测试了一个 256*256 的矩阵M的Jodan分解一直得到不到结果
        	需要确认的问题：（咨询冯元老师）
        		1. Jordan分解求解复杂度是否很高，我们遇到的得不到结果的现象是否正常？
        		2. 这里（红框）是否由替代实现方案？
        （注意：冯老师文档中提到的算法2对应下图论文里的算法1）

```
### 参考图片
![](https://github.com/gaochong1111/epmc/raw/master/doc/pics/Q1.jpg)

