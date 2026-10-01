package com.hook.vip.app;

import com.hook.vip.util.ReflectUtil;
import com.hook.vip.util.XposedUtil;

import java.lang.reflect.Method;

/**
 * 一木清单（com.wangc.todolist，无加固）的 hook。
 */
public class YimuListAppHooker {

    private static final String TAG = "kong";
    private static final String USER_CLASS = "com.wangc.todolist.database.entity.User";

    public YimuListAppHooker(ClassLoader classLoader) {
        VIPHook(classLoader);
    }

    private static void VIPHook(ClassLoader classLoader) {
        try {
            Method getMemberType = ReflectUtil.findMethod(USER_CLASS, classLoader, "getMemberType");
            final Object fixed = ReflectUtil.coerce(getMemberType.getReturnType(), "PERMANENT");

            XposedUtil.hook(getMemberType, "yimulist_get_member_type", chain -> {
                Object self = chain.getThisObject();
                XposedUtil.d("====================");
                XposedUtil.d("[User.getMemberType] BEFORE");
                if (self != null) {
                    try {
                        Object userId = ReflectUtil.callMethod(self, "getUserId");
                        Object nickName = ReflectUtil.callMethod(self, "getNickName");
                        XposedUtil.d("用户ID: " + userId);
                        XposedUtil.d("昵称: " + nickName);
                    } catch (Throwable t) {
                        XposedUtil.d("获取用户信息异常: " + t);
                    }
                }

                Object original = chain.proceed();

                XposedUtil.d("[User.getMemberType] AFTER");
                XposedUtil.d("原始返回值: " + original + " -> " + fixed);
                return fixed;
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook User.getMemberType 失败", t);
        }
    }
}
