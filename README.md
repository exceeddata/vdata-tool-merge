# vdata-tool-merge

## Introduction
Merge EXD vData files into larger size for more efficient processing in the cloud environment.  EXD vData is an edge database running on vehicles' domain controllers.  It stores signal data in a high-compression file format with the extension of .vsw.  EXD vData SDK offers vsw decoding capabilities in standard programming languages such as C++, [Java](https://github.com/exceeddata/vdata-sdk-java-samples), [Python](https://github.com/exceeddata/vdata-sdk-python-samples), [Javascript](https://github.com/exceeddata/vdata-sdk-javascript-samples), and etc.

The following sections demonstrates how to install and use the VSW .

## Table of Contents
- [System Requirement](#system-requirement)
- [Software Dependendies](#software-dependencies)
- [License](#license)
- [Demo Main Program](#demo-main-program)
- [Getting Help](#getting-help)
- [Contributing to EXD](#contributing-to-exd)

## System Requirement
- JDK 8

## Software Dependencies
- EXD vData SDK for Java 2.9.0+

## License
The codes in the repository are released with [MIT License](LICENSE).

## Demo Main Program

- mvn package to compile and package a jar class
- Prepare a folder of VSW files with the naming conversion vin_xxxxxx.vsw (or use the ones in the [data](data/) folder)
- Run the MergeFiles main(), provide the following required parameters
  - -i input_paths (may be comma-separated file paths or folder paths)
  - -o output_path (a output merged file path)

## Getting Help
For usage questions, the best place to go to is [Github issues](https://github.com/exceeddata/vdata-tool-merge/issues). For customers of EXCEEDDATA commercial solutions, you can contact [support](mailto:support@smartsct.com) for questions or support.

## Contributing to EXD
All contributions, bug reports, bug fixes, documentation improvements, code enhancements, and new ideas are welcome.

<hr>

[Go to Top](#table-of-contents)
