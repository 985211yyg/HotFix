package com.example.yyg.hotfix;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 热修复工具(只认后缀是dex、apk、jar、zip的补丁)
 */
public class FixUtils {
    private static final String DEX_SUFFIX = ".dex";
    private static final String APK_SUFFIX = ".apk";
    private static final String JAR_SUFFIX = ".jar";
    private static final String ZIP_SUFFIX = ".zip";
    public static final String DEX_DIR = "odex";
    private static final String OPTIMIZE_DEX_DIR = "optimize_dex";
    private static HashSet<File> loadedDex = new HashSet<>();

    static {
        loadedDex.clear();
    }

    /**
     * 加载补丁文件
     * 使用默认目录：data/data/包名/files/odex
     *
     * @param context
     */
    public static void loadFixedDex(Context context) throws ClassNotFoundException,
            NoSuchFieldException, IllegalAccessException {
        loadFixedDex(context, null);
    }

    /**
     * 从指定文件中加载补丁文件
     *
     * @param context       上下文
     * @param patchFileDirs 补丁文件路径
     */
    public static void loadFixedDex(Context context, File patchFileDirs) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        if (context == null) {
            return;
        }
        //遍历所有修复的dex
        File fileDir = patchFileDirs != null ? patchFileDirs : new File(context.getFilesDir(), DEX_DIR);
        File[] files = fileDir.listFiles();
        for (File file : files) {
            //判断文件
            if (file.getName().startsWith("classes") && (file.getName().endsWith(DEX_SUFFIX)
                    || (file.getName().endsWith(ZIP_SUFFIX)
                    || (file.getName().endsWith(APK_SUFFIX)
                    || file.getName().endsWith(JAR_SUFFIX))))) {
                loadedDex.add(file);//加入集合
            }
            //合并之前的dex
            doDexInject(context, loadedDex);
        }

    }

    /**
     * 合并dex
     *
     * @param context
     * @param loadedDex
     */
    private static void doDexInject(Context context, HashSet<File> loadedDex) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        //解压dex的路径
        String optimizeDir = context.getFilesDir().getAbsolutePath() + File.separator + OPTIMIZE_DEX_DIR;
        File optimizeFile = new File(optimizeDir);
        if (!optimizeFile.exists()) {
            optimizeFile.mkdirs();
        }
        //加载应用程序的的dex
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClass().getClassLoader();
        for (File dex : loadedDex) {
            DexClassLoader dexClassLoader = new DexClassLoader(dex.getAbsolutePath()//补丁所在目录
                    , optimizeFile.getAbsolutePath()//存放dex的解压目录
                    , null//
                    , pathClassLoader);//父类加载器

            //获取个部分数据
            Object dexPathList = getPathList(dexClassLoader);//补丁DexPathList
            Object pathPathList = getPathList(pathClassLoader);//app DexPathList
            //获取DexPathList中的DexElements
            Object leftDexElements = getDexElements(dexPathList);
            //获取app中的dexElements
            Object rightDexElements = getDexElements(pathPathList);
            //合并
            Object dexElements = combineArray(leftDexElements, rightDexElements);
            //重新赋值给PathList里面的Element[] dexElements
            Object pathList = getPathList(pathClassLoader);//重新获取
            setField(pathList, pathList.getClass(), "dexElements", dexElements);
        }
    }

    /**
     * 反射给对象中的属性重新赋值
     *
     * @param o      对象
     * @param aClass 类
     * @param field  属性
     * @param value  值
     */
    private static void setField(Object o, Class<?> aClass, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
            //反射获取到dexElements变量并赋值给它
            Field declaredField = aClass.getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(o, value);
    }

    /**
     * 反射获取对象的属性
     *
     * @param o      对象
     * @param aClass 类
     * @param field  属性
     */
    private static Object getField(Object o, Class<?> aClass, String field) throws NoSuchFieldException, IllegalAccessException {
        //获取该类中制定名称的成员变量
        Field localField = aClass.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(o);//返回字段代表的值
    }

    /**
     * 反射获取类加载器中的pathList对象
     *
     * @param classLoader
     * @return
     */
    private static Object getPathList(BaseDexClassLoader classLoader) throws ClassNotFoundException,
            NoSuchFieldException, IllegalAccessException {
        return getField(classLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 反射获取pathList中的dexElements
     * @param pathList
     * @return
     */
    private static Object getDexElements(Object pathList) throws NoSuchFieldException, IllegalAccessException {
        return getField(pathList,pathList.getClass(),"dexElements");
    }

    /**
     * 数组合并
     *
     * @param leftDexElements
     * @param rightDexElements
     * @return
     */
    private static Object combineArray(Object leftDexElements, Object rightDexElements) {
        Class<?> componentType = leftDexElements.getClass().getComponentType();
        int left = Array.getLength(leftDexElements);
        int right = Array.getLength(rightDexElements);
        //创建与DexElements类型一致的新数组
        Object result = Array.newInstance(componentType, left + right);
        //拷贝
        System.arraycopy(leftDexElements, 0, result, 0, left);
        System.arraycopy(rightDexElements, 0, result, left, right);
        return result;
    }
}
