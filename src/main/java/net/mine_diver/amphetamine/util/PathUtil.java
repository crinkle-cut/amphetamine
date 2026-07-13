package net.mine_diver.amphetamine.util;

public class PathUtil {
    public static String getPosixFullPath(String path) {
        if (path.startsWith("/"))
            return path;
        return "/" + path;
    }

    public static String normalizeToPosix(String path) {
        // Normalize path separators and resolve . and ..
        path = path.replace('\\', '/');

        // Split and rebuild
        String[] parts = path.split("/");
        java.util.Deque<String> stack = new java.util.ArrayDeque<>();

        for (String part : parts) {
            if (part.isEmpty() || part.equals("."))
                continue;
            if (part.equals("..")) {
                if (!stack.isEmpty())
                    stack.removeLast();
            } else {
                stack.addLast(part);
            }
        }

        StringBuilder result = new StringBuilder();
        if (path.startsWith("/"))
            result.append("/");
        result.append(String.join("/", stack));
        return result.toString();
    }
}
