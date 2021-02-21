package party.liyin.beanmapper;

import javassist.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BeanMapperInPlace {
    public static class TypePair {
        private final String name;
        private final Class<?> type;
        public TypePair(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypePair typePair = (TypePair) o;
            return name.equals(typePair.name) &&
                    type.equals(typePair.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }
    }
    public static class DataPair {
        private final String name;
        private final Object data;
        public DataPair(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        public String getName() {
            return name;
        }

        public Object getData() {
            return data;
        }
    }
    private static final HashMap<String, Object> cache = new HashMap<>();
    private String sourceClass = "";
    private Object mapper = null;
    public BeanMapperInPlace(Class<?> source, List<TypePair> typePairs) throws BeanMapperException {
        try {
            compile(source, typePairs);
        } catch (CannotCompileException | InstantiationException | NotFoundException | IllegalAccessException e) {
            e.printStackTrace();
            throw new BeanMapperException(e);
        }
    }
    private void compile(Class<?> source, List<TypePair> typePairs) throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException {
        sourceClass = source.getName();
        String className = ("V" + (source.getName() + " " + getFullUniqueName(typePairs)).hashCode()).replace("-", "U");
        if (cache.containsKey(className)) {
            mapper = cache.get(className);
        } else {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.makeClass("party.liyin.vdynclass." + className);
            CtClass iClass = pool.get(IBeanVirtual.class.getName());
            CtClass sClass = pool.get(source.getName());
            cc.addInterface(iClass);
            cc.setSuperclass(sClass);

            CtMethod tMethod = new CtMethod(pool.get("java.lang.Object"), "generate", new CtClass[] { pool.get("java.lang.Object") }, cc);
            CtMethod wMethod = new CtMethod(CtClass.voidType, "doGenerate", new CtClass[] { sClass, cc }, cc);
            CtConstructor constructor = new CtConstructor(null, cc);
            constructor.setBody("{}");
            cc.addConstructor(constructor);
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            Field[] sField = source.getDeclaredFields();
            for (Field field: sField) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    String uField = StaticBeanMapper.toFirstUpper(field.getName());
                    builder.append("$2.set").append(uField).append("($1.get").append(uField).append("());");
                }
            }
            builder.append("}");
            wMethod.setBody(builder.toString());
            cc.addMethod(wMethod);
            tMethod.setBody("{ " + cc.getName() + " target = new " + cc.getName() +"(); doGenerate((" + source.getName() +")$1, target); return target; }");
            cc.addMethod(tMethod);
            for (TypePair typePair : typePairs) {
                CtField cf = new CtField(pool.get(typePair.type.getName()), typePair.name, cc);
                cf.setModifiers(Modifier.PRIVATE);
                cc.addField(cf);
                cc.addMethod(CtNewMethod.getter("get" + StaticBeanMapper.toFirstUpper(typePair.name), cf));
                cc.addMethod(CtNewMethod.setter("set" + StaticBeanMapper.toFirstUpper(typePair.name), cf));
            }

            CtMethod setterMethod = new CtMethod(CtClass.voidType, "_setVirtual_", new CtClass[] { pool.get("java.lang.String"), pool.get("java.lang.Object") }, cc);
            StringBuilder vBuilder = new StringBuilder();
            vBuilder.append("{\n");
            vBuilder.append("switch($1.hashCode()) {\n");
            for (TypePair typePair : typePairs) {
                vBuilder.append("case ").append(typePair.name.hashCode()).append(":\n");
                String value = "";
                if (typePair.type.isPrimitive()) {
                    if (Boolean.TYPE.equals(typePair.type)) {
                        value = "((java.lang.Boolean)$2).booleanValue()";
                    } else if (Character.TYPE.equals(typePair.type)) {
                        value = "((java.lang.Character)$2).charValue()";
                    } else if (Byte.TYPE.equals(typePair.type)) {
                        value = "((java.lang.Byte)$2).byteValue()";
                    } else if (Short.TYPE.equals(typePair.type)) {
                        value = "((java.lang.Short)$2).shortValue()";
                    } else if (Integer.TYPE.equals(typePair.type)) {
                        value = "((java.lang.Integer)$2).intValue()";
                    } else if (Long.TYPE.equals(typePair.type)) {
                        value = "((java.lang.Long)$2).longValue()";
                    } else if (Float.TYPE.equals(typePair.type)) {
                        value = "((java.lang.Float)$2).floatValue()";
                    } else if (Double.TYPE.equals(typePair.type)) {
                        value = "((java.lang.Double)$2).doubleValue()";
                    }
                } else {
                    value = "(" + typePair.type.getName() + ")$2";
                }
                vBuilder.append("{ ").append("set").append(StaticBeanMapper.toFirstUpper(typePair.name)).append("(").append(value).append("); break; }\n");
            }
            vBuilder.append("default:{}\n");
            vBuilder.append("}");
            vBuilder.append("}");
            setterMethod.setBody(vBuilder.toString());
            cc.addMethod(setterMethod);

            cc.detach();
            mapper = cc.toClass().newInstance();
            cache.put(className, mapper);
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T copy(T source, List<DataPair> dataPairs) {
        IBeanVirtual bean = (IBeanVirtual) ((IBeanVirtual)mapper).generate(source);
        for (DataPair dataPair : dataPairs) {
            bean._setVirtual_(dataPair.name, dataPair.data);
        }
        return (T)bean;
    }
    private static String getFullUniqueName(List<TypePair> typePairs) {
        StringBuilder builder = new StringBuilder();
        List<TypePair> sorted = typePairs.stream().sorted().collect(Collectors.toList());
        for (TypePair typePair : sorted) {
            builder.append(typePair.name).append("-").append(typePair.type.getName()).append(",");
        }
        return builder.toString();
    }
}
