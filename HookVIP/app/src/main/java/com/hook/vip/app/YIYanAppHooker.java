package com.hook.vip.app;

import com.hook.vip.util.ReflectUtil;
import com.hook.vip.util.XposedUtil;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

/**
 * 一言（com.jhyan.yan，360 加固）的 hook。
 */
public class YIYanAppHooker {

    private static final String TAG = "kong";
    private static final String USER_CLASS = "com.jasonhan.GongMing.Beans.User";

    public static void hook(ClassLoader classLoader) {
        Vip(classLoader);
    }

    private static void Vip(ClassLoader classLoader) {
        try {
            Method isVip = ReflectUtil.findMethod(USER_CLASS, classLoader, "isVip");
            XposedUtil.hook(isVip, "yiyan_is_vip", chain -> {
                Object original = chain.proceed();
                XposedUtil.d("[isVip] 返回值: " + original + " -> true");
                return Boolean.TRUE;
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook User.isVip 失败", t);
        }

        try {
            Method fillVipData = ReflectUtil.findMethod(USER_CLASS, classLoader, "fillVipData", Map.class);
            XposedUtil.hook(fillVipData, "yiyan_fill_vip_data", chain -> {
                Object arg = chain.getArg(0);
                if (arg instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) arg;
                    map.put("viptype", 4);
                    map.put("vipdate", new Date());
                    XposedUtil.d("fillVipData: 已注入终身会员数据");
                }
                return chain.proceed();
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook User.fillVipData 失败", t);
        }
    }
}
