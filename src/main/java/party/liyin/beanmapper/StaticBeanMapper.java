package party.liyin.beanmapper;

import javassist.*;

import java.lang.reflect.Field;
import java.util.HashMap;

public class StaticBeanMapper {
    private static final HashMap<String, Object> classMap = new HashMap<>();
    protected static String toFirstUpper(String str) {
        if (str.length() > 0) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        } else {
            return str;
        }
    }
    public static void copy(Object source, Object target) throws BeanMapperException {
        try {
            String className = "T" + (source.getClass().getName() + " " + target.getClass().getName()).hashCode();
            Object clazz;
            if (classMap.containsKey(className)) {
                clazz = classMap.get(className);
            } else {
                ClassPool pool = ClassPool.getDefault();
                CtClass cc = pool.makeClass("party.liyin.sdynclass." + className);
                CtClass iClass = pool.get(BeanCopyable.class.getName());
                cc.addInterface(iClass);
                CtMethod tMethod = new CtMethod(CtClass.voidType, "copy", new CtClass[] { pool.get("java.lang.Object"), pool.get("java.lang.Object") }, cc);
                CtClass sClass = pool.get(source.getClass().getName());
                CtClass tClass = pool.get(target.getClass().getName());
                CtMethod wMethod = new CtMethod(CtClass.voidType, "doCopy", new CtClass[] { sClass, tClass }, cc);
                StringBuilder builder = new StringBuilder();
                builder.append("{");
                Field[] sField = source.getClass().getDeclaredFields();
                for (Field field: sField) {
                    String uField = toFirstUpper(field.getName());
                    builder.append("$2.set").append(uField).append("($1.get").append(uField).append("());");
                }
                builder.append("}");
                wMethod.setBody(builder.toString());
                cc.addMethod(wMethod);
                tMethod.setBody("{ doCopy((" + source.getClass().getName() +")$1, (" + target.getClass().getName() + ")$2); }");
                cc.addMethod(tMethod);
                cc.detach();
                clazz = cc.toClass().newInstance();
                classMap.put(className, clazz);
            }
            ((BeanCopyable)clazz).copy(source, target);
        } catch (CannotCompileException | NotFoundException | IllegalAccessException | InstantiationException e) {
            throw new BeanMapperException(e);
        }
    }
}
