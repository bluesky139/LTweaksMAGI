package li.lingfeng.magi.utils;

import android.text.DynamicLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ReflectUtils {

    private static Method setHiddenApiExemptionsMethod;
    private static Object sVmRuntime;
    private static HashSet<String> apiExemptions = new HashSet<>();
    private static HashMap<String, Field> fieldCache = new HashMap<>();
    private static HashMap<String, Method> methodCache = new HashMap<>();

    public static void addHiddenApiExemptions(String... signatures) {
        for (String signature : signatures) {
            apiExemptions.add(signature);
        }
        try {
            // https://github.com/tiann/FreeReflection/blob/master/library/src/main/java/me/weishu/reflection/BootstrapClass.java
            if (setHiddenApiExemptionsMethod == null) {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                setHiddenApiExemptionsMethod = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
                sVmRuntime = getRuntime.invoke(null);
            }
            setHiddenApiExemptionsMethod.invoke(sVmRuntime, new Object[]{apiExemptions.toArray(new String[0])});
        } catch (Throwable e) {
            Logger.e("Failed to setHiddenApiExemptions.", e);
        }
    }

    public static Object callMethod(Object obj, String methodName, Object... args) throws Throwable {
        Method method = findMethod(obj.getClass(), methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    public static Object callMethod(Object obj, String methodName, Object[] args, Class[] parameterTypes) throws Throwable {
        Method method = findMethod(obj.getClass(), methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    public static Object callStaticMethod(Class cls, String methodName, Object... args) throws Throwable {
        Method method = findMethod(cls, methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    public static Object callStaticMethod(Class cls, String methodName, Object[] args, Class[] parameterTypes) throws Throwable {
        Method method = findMethod(cls, methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    public static Method findMethod(Class cls, String methodName, Class[] parameterTypes) throws Throwable {
        StringBuilder builder = new StringBuilder();
        builder.append(cls.getName());
        builder.append('#');
        builder.append(methodName);
        builder.append('(');
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(parameterTypes[i].getName());
        }
        builder.append(')');
        String fullMethodName = builder.toString();
        Method method = methodCache.get(fullMethodName);
        if (method == null) {
            method = _findMethod(cls, methodName, parameterTypes);
            method.setAccessible(true);
            methodCache.put(fullMethodName, method);
        }
        return method;
    }

    private static Method _findMethod(Class cls, String methodName, Class[] parameterTypes) throws Throwable {
        addHiddenApiExemptions("L" + cls.getName().replace('.', '/') + ';');
        try {
            return cls.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            while (true) {
                cls = cls.getSuperclass();
                if (cls == null || cls.equals(Object.class)) {
                    throw e;
                }
                try {
                    return cls.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e1) {
                }
            }
        }
    }

    public static Object getObjectField(Object obj, String fieldName) throws Throwable {
        return findField(obj.getClass(), fieldName).get(obj);
    }

    public static Object getStaticObjectField(Class cls, String fieldName) throws Throwable {
        return findField(cls, fieldName).get(null);
    }

    public static boolean getBooleanField(Object obj, String fieldName) throws Throwable {
        return findField(obj.getClass(), fieldName).getBoolean(obj);
    }

    public static int getIntField(Object obj, String fieldName) throws Throwable {
        return findField(obj.getClass(), fieldName).getInt(obj);
    }

    public static float getFloatField(Object obj, String fieldName) throws Throwable {
        return findField(obj.getClass(), fieldName).getFloat(obj);
    }

    public static void setObjectField(Object obj, String fieldName, Object value) throws Throwable {
        findField(obj.getClass(), fieldName).set(obj, value);
    }

    public static void setStaticObjectField(Class cls, String fieldName, Object value) throws Throwable {
        findField(cls, fieldName).set(null, value);
    }

    public static void setIntField(Object obj, String fieldName, int value) throws Throwable {
        findField(obj.getClass(), fieldName).setInt(obj, value);
    }

    public static void setBooleanField(Object obj, String fieldName, boolean value) throws Throwable {
        findField(obj.getClass(), fieldName).setBoolean(obj, value);
    }

    public static void setStaticBooleanField(Class cls, String fieldName, boolean value) throws Throwable {
        findField(cls, fieldName).setBoolean(null, value);
    }

    public static Field findField(Class cls, String fieldName) throws Throwable {
        StringBuilder builder = new StringBuilder();
        builder.append(cls.getName());
        builder.append('#');
        builder.append(fieldName);
        String fullFieldName = builder.toString();
        Field field = fieldCache.get(fullFieldName);
        if (field == null) {
            field = _findField(cls, fieldName);
            field.setAccessible(true);
            fieldCache.put(fullFieldName, field);
        }
        return field;
    }

    private static Field _findField(Class cls, String fieldName) throws Throwable {
        try {
            return cls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            while (true) {
                cls = cls.getSuperclass();
                if (cls == null || cls.equals(Object.class)) {
                    throw e;
                }
                try {
                    return cls.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e1) {
                }
            }
        }
    }

    public static Field findFirstFieldByExactType(Class cls, Class type) throws Throwable {
        Class _cls = cls;
        do {
            Field[] fields = _cls.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == type) {
                    field.setAccessible(true);
                    return field;
                }
            }
            _cls = _cls.getSuperclass();
            if (_cls == null || _cls.equals(Object.class)) {
                throw new NoSuchFieldError("findFirstFieldByExactType " + type + " in " + cls);
            }
        } while (true);
    }

    public static Method findFirstMethodByTypes(Class cls, Class[] parameterTypes, Class returnType) {
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getParameterCount() != parameterTypes.length || !returnType.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            Class[] types = method.getParameterTypes();
            boolean match = true;
            for (int i = 0; i < types.length; ++i) {
                if (!parameterTypes[i].isAssignableFrom(types[i])) {
                    match = false;
                    continue;
                }
            }
            if (!match) {
                continue;
            }
            method.setAccessible(true);
            return method;
        }
        return null;
    }

    public static void printFields(Object obj) throws Throwable {
        Field[] fields = obj.getClass().getFields();
        for (Field field : fields) {
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(obj);
            Logger.v(" field[" + field.getName() + "] " + value
                    + (value == null ? "" :
                     (field.getType() == DynamicLayout.class ? " " + getObjectField(value, "mBase") : ""))
                    );
        }
    }

    public static Object getSurroundingThis(Object obj) throws Throwable {
        String name = obj.getClass().getName();
        int pos = name.lastIndexOf('$');
        if (pos > 0) {
            name = name.substring(0, pos);
            Class cls = findClass(name, obj.getClass().getClassLoader());
            Field field = findFirstFieldByExactType(obj.getClass(), cls);
            return field.get(obj);
        }
        return null;
    }

    public static Constructor getFirstConstructor(Class cls) {
        Constructor constructor = cls.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor;
    }

    public static Constructor getConstructor(Class cls, Class... parameterTypes) throws NoSuchMethodException {
        Constructor constructor = cls.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor;
    }

    public static Object newInstance(Class cls, Object... args) throws Throwable {
        Constructor constructor = getConstructor(cls, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        return constructor.newInstance(args);
    }

    public static Object newInstance(Class cls, Object[] args, Class[] parameterTypes) throws Throwable {
        Constructor constructor = getConstructor(cls, parameterTypes);
        return constructor.newInstance(args);
    }

    public static Class findClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(className, false, classLoader);
    }
}
