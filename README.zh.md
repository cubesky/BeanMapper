# BeanMapper
一个 BeanUtils.copyProperties 的小型快速替代。

## 起因
由于 BeanUtils （Spring 或 Apache Commons） 的 copyProperties 实现是利用反射实现的，它在大量调用时具有比较严重的性能问题。

BeanMapper 通过 javassist 类库实现在运行时直接生成字节码来转换类，在首次构建后，后续运行可以提供近原生的读写速度。

## 限制
 * 类必须具有符合小驼峰规范的 JavaBean setter 和 getter。否则无法正常访问类。
 * 类必须是公共可见的，否则会发生访问错误。

## 导入
预编译包会被 CI 自动发布到 Github Package 上，直接从 Github Package 引用即可。

## 使用
BeanMapper 库提供两个 BeanMapper 供您使用。

### StaticBeanMapper
StaticBeanMapper 仅需要您传入需要进行复制的原始类和目标类实例即可自动转换。

```java
StaticBeanMapper.copy(source, target);
```

StaticBeanMapper 使用一个 HashMap 和类的全名来维护转换类列表，自动调用对应的转换类。这会导致一定的性能损失，但损失仍远低于反射方式。

### BeanMapper
BeanMapper 需要您创建实例来进行维护，一个 BeanMapper 实例仅能对应一对原始类和目标类的转换。

建议将处理每对 Bean 的 BeanMapper 声明为静态终态变量(static final)。

避免多次自动创建类造成冲突崩溃。

BeanMapper 有2个构造函数，当使用无参构造方法时，转换类生成工作将会被推迟到首次使用时。
```java
BeanMapper mapper = new BeanMapper();
mapper.copy(source, target)
```

您也可以使用有参构造方法，在构造时进行创建。
```java
BeanMapper mapper = new BeanMapper(Source.class, Target.class);
mapper.copy(source, target)
```
## 效率
在 10000000 次复制测试中

BeanUtils.copyProperties 用时 9809 毫秒

StaticBeanMapper.copy 用时 100毫秒（首次编译）+ 3633 毫秒（后续 10000000 次调用）

BeanMapper.copy 用时 98毫秒（首次编译）+ 1641 毫秒（后续 10000000 次调用）

手写 setter 用时 1479 毫秒