package com.yyblcc.ecommerceplatforms.util.commentPath;

public class PathUtils {

    // 生成根评论 path（maxRoot = null 表示没有任何根评论）
    public static String genRootNextPath(String maxRootPath) {
        if (maxRootPath == null) {
            return formatNumber(1);
        }
        int last = Integer.parseInt(maxRootPath);
        return formatNumber(last + 1);
    }

    // 生成子评论 path
    public static String genChildNextPath(String parentPath, String maxChildPath) {
        if (maxChildPath == null) {
            return parentPath + "." + formatNumber(1);
        }

        String[] arr = maxChildPath.split("\\.");
        int last = Integer.parseInt(arr[arr.length - 1]);
        return parentPath + "." + formatNumber(last + 1);
    }

    // 格式化为 4 位（0001、0023）
    private static String formatNumber(int num) {
        return String.format("%04d", num);
    }
}
