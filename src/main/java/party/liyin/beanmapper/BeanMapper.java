package party.liyin.beanmapper;

import javassist.*;

import java.lang.reflect.Field;
import java.util.HashMap;

public class BeanMapper {
    private static final HashMap<String, Object> cache = new HashMap<>();
    private String sourceClass = "";
    private String targetClass = "";
    private Object mapper = null;
    public BeanMapper(){}
    public BeanMapper(Class<?> source, Class<?> target) throws BeanMapperException {
        try {
            compile(source, target);
        } catch (CannotCompileException | InstantiationException | NotFoundException | IllegalAccessException e) {
            throw new BeanMapperException(e);
        }

    }
    private void compile(Class<?> source, Class<?> target) throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException {
        sourceClass = source.getName();
        targetClass = target.getName();
        String className = "T" + (source.getName() + " " + target.getName()).hashCode();
        if (cache.containsKey(className)) {
            mapper = cache.get(className);
        } else {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.makeClass("party.liyin.dynclass." + className);
            CtClass iClass = pool.get(BeanCopyable.class.getName());
            cc.addInterface(iClass);
            CtMethod tMethod = new CtMethod(CtClass.voidType, "copy", new CtClass[] { pool.get("java.lang.Object"), pool.get("java.lang.Object") }, cc);
            CtClass sClass = pool.get(source.getName());
            CtClass tClass = pool.get(target.getName());
            CtMethod wMethod = new CtMethod(CtClass.voidType, "doCopy", new CtClass[] { sClass, tClass }, cc);
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            Field[] sField = source.getDeclaredFields();
            for (Field field: sField) {
                String uField = StaticBeanMapper.toFirstUpper(field.getName());
                builder.append("$2.set").append(uField).append("($1.get").append(uField).append("());");
            }
            builder.append("}");
            wMethod.setBody(builder.toString());
            cc.addMethod(wMethod);
            tMethod.setBody("{ doCopy((" + source.getName() +")$1, (" + target.getName() + ")$2); }");
            cc.addMethod(tMethod);
            cc.detach();
            mapper = cc.toClass().newInstance();
            cache.put(className, mapper);
        }
    }

    public void copy(Object source, Object target) throws BeanMapperException {
        try {
            if (mapper != null) {
                if (!sourceClass.equals(source.getClass().getName()) || !targetClass.equals(target.getClass().getName())) {
                    throw new BeanMapperException(new IllegalArgumentException());
                }
            } else {
                compile(source.getClass(), target.getClass());
            }
            ((BeanCopyable)mapper).copy(source, target);
        } catch (CannotCompileException | NotFoundException | IllegalAccessException | InstantiationException e) {
            throw new BeanMapperException(e);
        }
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getTargetClass() {
        return targetClass;
    }
}
