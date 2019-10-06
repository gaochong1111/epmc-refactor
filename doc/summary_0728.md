# ltl 到 PA 转换的探索
## 调通epmc中的JUnit测试框架
- 可以更高效地定位问题
- 可以进行模块化测试
- 需要进一步熟悉测试框架，以具备独立写测试模块的能力
## 定位 epmc 中使用过此过程的模块
- propertySolverDDCoalition.solve()->buildGame()->newAutomatonParity() 方法中使用 (这里被李勇师兄否定，没有做进一步探索)
- 下一步会探索，spot直接命令生成pa描述文件，用HOAParser解析得到PA
    - 如果工具已实现，需要一个测试用例使得其求解包含改过程，通过调试熟悉使用方法，复用此过程
    - **这里我们需要一个可以运行的测试用例**
    - 如果工具未实现，修改HOAParser解析得到PA
## 明确需求: 我们需要了解目前epmc中已经实现的ltl到PA的实现过程，以及使用方法
- 实现过程: spot 生成 hoa格式的描述，HOAParser 解析得到(和李勇师兄讨论得到结论)
- 使用方法：暂未找到，**需要提供一个测试用例(模型描述+ltl公式)，可运行，并且在求解过程中使用到了ltl到PA的过程**


# getBSCC算法实现方案
## 第一步调研线性代数库
- 分别调研java的线性代数库Jblas、jama、apach common math、Eigen、Jeigen
    - java库对于复数矩阵的支持比较弱
    - c++库Eigen对于复数的支持良好，Jeigen是对其的封装 
    - 考虑到epmc直接使用Eigen和Jeigen的开发效率较低（接口），所以考虑将此过程与工具解耦
    - 使用python的库快速开发出独立可用的工具包以供epmc使用
        - 下一步考虑具体实现方案 

