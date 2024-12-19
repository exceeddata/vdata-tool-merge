# 1. 文档目的
帮助使用merge vsw，
1. 将多个VSW小文件合并成一个大的MVSW(Merge VSW)存储。
2. 解析合并MVSW文件，并通过SDK解析成原始的信号或报文。


# 2. 使用

## 2.1 合并VSW文件
示例详见MultiVDataFileTest.combineTest，这个单元测试会将指定目录中的vsw文件，按指定的VIN Pattern 重复生成若干次。


## 2.2 解析  合并VSW文件
示例详见MultiVDataFileTest.readMultiTest，这个单元测试用例解析输入的文件，并使用迭代器，依次读取合并文件中的原始VSW内容。


