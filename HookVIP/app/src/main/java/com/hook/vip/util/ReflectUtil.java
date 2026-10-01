package com.hook.vip.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 反射工具，替代旧的 {@code de.robv.android.xposed.XposedHelpers}。
 *
 * 模块目标 API 102，框架不允许再调用 legacy {@code de.robv.android.xposed} 包，
 * 所以 findClass / findMethod / callMethod 这些能力必须自带。
 */
public final class ReflectUtil {

    private ReflectUtil() {
    }

    // ------------------------------------------------------------ 类 / 方法

    public static Class<?> findClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        // initialize=false，避免提前触发目标类的静态初始化
        return Class.forName(className, false, classLoader);
    }

    /**
     * 按“方法名 + 参数类型”查找方法，找不到时沿父类继续找。
     * 参数类型可以是 {@link Class}，也可以是类名字符串或 {@code "int"} 这类基本类型名。
     */
    public static Method findMethod(String className, ClassLoader classLoader,
                                    String methodName, Object... parameterTypes) throws Exception {
        Class<?> clazz = findClass(className, classLoader);
        return findMethod(clazz, methodName, resolveTypes(parameterTypes, classLoader));
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        NoSuchMethodException notFound = null;
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                Method method = c.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                if (notFound == null) {
                    notFound = e;
                }
            }
        }
        // 兜底：按方法名 + 参数个数 + 参数类型名匹配，应对加固壳的签名差异
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != parameterTypes.length) {
                    continue;
                }
                if (!sameTypeNames(method.getParameterTypes(), parameterTypes)) {
                    continue;
                }
                method.setAccessible(true);
                return method;
            }
        }
        throw notFound != null ? notFound
                : new NoSuchMethodException(clazz.getName() + "." + methodName);
    }

    /** 按方法名 + 参数个数查找（用于参数类型不好写死的场景）。 */
    public static Method findMethodByName(Class<?> clazz, String methodName, int parameterCount)
            throws NoSuchMethodException {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new NoSuchMethodException(clazz.getName() + "." + methodName + "/" + parameterCount);
    }

    // ------------------------------------------------------------ 调用

    /** 调用实例方法，按实参运行时类型匹配（等价于旧的 {@code XposedHelpers.callMethod}）。 */
    public static Object callMethod(Object obj, String methodName, Object... args) throws Exception {
        if (obj == null) {
            throw new NullPointerException("callMethod on null receiver for " + methodName);
        }
        Method method = findMethodByArgs(obj.getClass(), methodName, args);
        return method.invoke(obj, args);
    }

    /** 调用静态方法（等价于旧的 {@code XposedHelpers.callStaticMethod}）。 */
    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) throws Exception {
        Method method = findMethodByArgs(clazz, methodName, args);
        return method.invoke(null, args);
    }

    /** 调用无参构造器创建实例。 */
    public static Object newInstance(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    public static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // 继续往父类找
            }
        }
        throw new NoSuchFieldException(clazz.getName() + "." + fieldName);
    }

    // ------------------------------------------------------------ 返回值转换

    /**
     * 把常量转换成目标方法声明的返回类型，避免出现 {@code Integer} 塞进 {@code boolean}
     * 这类 ClassCastException。不适用的组合原样返回。
     */
    public static Object coerce(Class<?> returnType, Object value) {
        if (value == null || returnType == null || returnType == void.class) {
            return value;
        }
        if ((returnType == boolean.class || returnType == Boolean.class) && value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if ((returnType == int.class || returnType == Integer.class) && value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        if ((returnType == long.class || returnType == Long.class)
                && value instanceof Number && !(value instanceof Long)) {
            return ((Number) value).longValue();
        }
        return value;
    }

    // ------------------------------------------------------------ 内部实现

    private static Class<?>[] resolveTypes(Object[] types, ClassLoader classLoader) throws ClassNotFoundException {
        if (types == null || types.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] resolved = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            resolved[i] = resolveType(types[i], classLoader);
        }
        return resolved;
    }

    private static Class<?> resolveType(Object type, ClassLoader classLoader) throws ClassNotFoundException {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof String) {
            String name = (String) type;
            switch (name) {
                case "boolean":
                    return boolean.class;
                case "byte":
                    return byte.class;
                case "char":
                    return char.class;
                case "short":
                    return short.class;
                case "int":
                    return int.class;
                case "long":
                    return long.class;
                case "float":
                    return float.class;
                case "double":
                    return double.class;
                case "void":
                    return void.class;
                default:
                    return Class.forName(name, false, classLoader);
            }
        }
        throw new IllegalArgumentException("不支持的参数类型: " + type);
    }

    private static Method findMethodByArgs(Class<?> clazz, String methodName, Object[] args) throws NoSuchMethodException {
        int count = args == null ? 0 : args.length;
        List<Method> candidates = new ArrayList<>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == count) {
                    candidates.add(method);
                }
            }
        }
        for (Method method : candidates) {
            if (argsMatch(method.getParameterTypes(), args, true)) {
                method.setAccessible(true);
                return method;
            }
        }
        for (Method method : candidates) {
            if (argsMatch(method.getParameterTypes(), args, false)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(clazz.getName() + "." + methodName + "/" + count);
    }

    private static boolean argsMatch(Class<?>[] parameterTypes, Object[] args, boolean strict) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameter = parameterTypes[i];
            Object arg = args[i];
            if (arg == null) {
                if (parameter.isPrimitive()) {
                    return false;
                }
                continue;
            }
            Class<?> boxed = wrap(parameter);
            if (boxed.equals(arg.getClass())) {
                continue;
            }
            if (strict) {
                return false;
            }
            if (!boxed.isInstance(arg) && !(arg instanceof Number && Number.class.isAssignableFrom(boxed))) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameTypeNames(Class<?>[] actual, Class<?>[] expected) {
        if (actual.length != expected.length) {
            return false;
        }
        for (int i = 0; i < actual.length; i++) {
            if (!actual[i].getName().equals(expected[i].getName())) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        return type;
    }
}
