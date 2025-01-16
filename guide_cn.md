# 1. 文档目的
VSW文件格式是EXD在车端进行高精数据采集存储的高压缩比的基于时间桶的列式存储文件。VSW通常在车端生成，通常半分钟到三分钟生成一个VSW，然后上传到云端。在量产车的云端系统中，通常会产生巨量的小文件。为了避免在云端存储巨量的小文件带来的存储空间利用率下降，以及检索不便的问题。EXD专门设计了对应的合并VSW格式，可以将多个VSW文件合并存储，高效的生成合并VSW格式。

本文档的主要目的介绍如何使用merge vsw，
1. 将多个VSW小文件合并成一个大的MVSW(Merge VSW)存储。
2. 解析合并MVSW文件，并通过SDK解析成原始的信号或报文。


# 2. 合并格式2.0版本的新功能
1. 支持Keys Block合并去重存储，针对于Keys Block占比较高的小VSW时，提供更高的存储效率，预计针对于22多K的vsw，少量增加合并CPU开销的基础上，可额外节省3-10%左右的存储； 

2. 支持Bucket Region，将原有的Bucket数据解压，按统一的Device Bucket时间分组重新压缩，本功能预计节省15-25%的存储空间，但是在合并时需要对数据进行解压，合并和二次压缩的过程，预计会增加合并所需要的CPU；

3. 支持按信号值(Int64/Float64)的 Min/Max统计信息功能，以便于Hive等大数据应用对于数据的预过滤；

# 3. 使用

## 3.1 合并VSW文件
示例详见MergeFiles.merge方法，这个单元测试会将指定目录中的vsw文件，按指定的VIN Pattern 重复生成若干次。
测试的vsw文件见 data目录下的文件。


## 3.2 解析  合并VSW文件
MVDataReaderFactory.openMultipleVswFormats方法将返回一个迭代器。
通过遍历迭代器，依次读取合并文件中的原始VSW内容。

## 3.3 vData SDK （> 2.9.7版本） 中提供的命令行合并工具 
com.exceeddata.sdk.vdata.app.Glance
支持三种模式：从VSW文件合并、打印合并文件的摘要信息、转换为CSV文件； 
java -cp vdata.jar com.exceeddata.sdk.vdata.app.Glance -i inputPath -m mode -o output
     [-i|input <paths>]. Required. The input vsw/mvsw file path(s). Multiple files are comma separated.
     [-o|output <path>]. Required. The output file path.
     [-m|mode <mode>]. Required. Working mode, default mode is dump summary for Merged file. Support mode: merge, dump, 2csv, signals
     [-s|signals <names>]. Optional. Comma-separated list of signal names. To generate statistic block while merging. Output signal list for 2csv mode
     [-d|device <device>]. Optional. device id (VIN) to decode
     [-l|length <int>]. Optional. Keep Device Id Length while merge.
     [-h|help]. optional)

合并VSW文件模式下支持的参数：
1. -i参数指定合并前VSW的目录。 合并前的VSW文件必须为VIN作为前缀，用半角的下划线区别VIN和之后的部分
2. -o参数 指定输出目录。 
3. -l参数 可以指定保留的VIN前缀长度，以标准17位的VIN为例，保留15意味着最多每100个VIN的数据合成一个文件
4. -s 参数 用于指定需要生成索引的信号，注意，只支持数值型的信号，不支持报文；

# 4. TODO List
1. 为了方便后续在vDataHive SDK的使用，在合并过程中，可以支持信号名替换；
2. 支持低频专用格式功能；
