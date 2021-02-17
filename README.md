# BeanMapper
一个 BeanUtils.copyProperties 的小型快速替代。

## 起因
由于 BeanUtils （Spring 或 Apache Commons） 的 copyProperties 实现是利用反射实现的，它在大量调用时具有比较严重的性能问题。

BeanMapper 通过 javassist 类库实现在运行时直接生成字节码来转换类，在首次构建后，后续运行可以提供近原生的读写速度。

## 限制
 * 类必须具有符合小驼峰规范的 JavaBean setter 和 getter。否则无法正常访问类。
 * 类必须是公共可见的，否则会发生访问错误。
 
## 效率
在 10000000 次复制测试中

BeanUtils.copyProperties 用时 9809 毫秒

StaticBeanMapper.copy 用时 100毫秒（首次编译）+ 3633 毫秒（后续 10000000 次调用）

BeanMapper.copy 用时 98毫秒（首次编译）+ 1641 毫秒（后续 10000000 次调用）

手写 setter 用时 1479 毫秒