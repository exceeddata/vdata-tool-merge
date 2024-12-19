# 1. 文档目的
帮助使用merge vsw，
1. 将多个VSW小文件合并成一个大的MVSW(Merge VSW)存储。
2. 解析合并MVSW文件，并通过SDK解析成原始的信号或报文。


# 2. 使用

## 2.1 合并VSW文件
示例详见MergeFiles.merge方法，这个单元测试会将指定目录中的vsw文件，按指定的VIN Pattern 重复生成若干次。
测试的vsw文件见 data目录下的文件。


## 2.2 解析  合并VSW文件
MVDataReaderFactory.openMultipleVswFormats方法将返回一个迭代器。
通过遍历迭代器，依次读取合并文件中的原始VSW内容。


