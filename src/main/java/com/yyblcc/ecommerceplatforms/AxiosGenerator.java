package com.yyblcc.ecommerceplatforms;

import lombok.Data;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileWriter;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class AxiosGenerator {
    private static final Map<String, List<Endpoint>> groupedEndpoints = new LinkedHashMap<>();
    private static final Map<String, String> typeDefinitions = new LinkedHashMap<>();
    private static final Map<String, String> classNameMap = new HashMap<>(); // 全路径名 -> TS类型名

    private static final Set<Class<?>> SIMPLE_TYPES = new HashSet<>(Arrays.asList(
            String.class, Integer.class, Long.class, Double.class, Float.class,
            Short.class, Byte.class, BigInteger.class, BigDecimal.class,
            int.class, long.class, double.class, float.class, short.class, byte.class,
            boolean.class, Boolean.class, void.class, Void.class
    ));

    public static void main(String[] args) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents("");

        candidateComponents.forEach(beanDefinition -> {
            try {
                generator(Class.forName(beanDefinition.getBeanClassName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        generateGAxiosFile();
        generateGTypeFile();
    }

    private static void generator(Class<?> restController) {
        String controllerName = restController.getSimpleName().replace("Controller", "");
        groupedEndpoints.putIfAbsent(controllerName, new ArrayList<>());

        String base = "";
        RequestMapping baseMapping = restController.getAnnotation(RequestMapping.class);
        if (baseMapping != null && baseMapping.value().length > 0) base = baseMapping.value()[0];

        for (Method method : restController.getDeclaredMethods()) {
            String httpMethod = getHttpMethod(method);
            String[] paths = getPaths(method);
            if (paths == null) continue;

            String tsReturnType = collectType(method.getGenericReturnType());

            for (String path : paths) {
                Endpoint endpoint = new Endpoint();
                endpoint.setMethodName(controllerName + "_" + method.getName());
                endpoint.setHttpMethod(httpMethod);
                endpoint.setFullPath((base + "/" + path).replaceAll("/+", "/"));
                endpoint.setReturnType(tsReturnType);

                for (Parameter p : method.getParameters()) {
                    processParameter(endpoint, p);
                }
                groupedEndpoints.get(controllerName).add(endpoint);
            }
        }
    }

    private static void processParameter(Endpoint endpoint, Parameter p) {
        Class<?> type = p.getType();
        Type genericType = p.getParameterizedType() != null ? p.getParameterizedType() : p.getType();
        String tsType = collectType(genericType);

        String paramName = p.getName();
        boolean hasRequestParam = p.isAnnotationPresent(RequestParam.class);
        if (hasRequestParam) {
            RequestParam rp = p.getAnnotation(RequestParam.class);
            if (!rp.value().isEmpty()) paramName = rp.value();
            else if (!rp.name().isEmpty()) paramName = rp.name();
        }

        if (p.isAnnotationPresent(PathVariable.class)) {
            endpoint.addPathParam(paramName, tsType);
        } else if (p.isAnnotationPresent(RequestBody.class)) {
            endpoint.setBodyParamName(paramName);
            endpoint.setBodyParamType(tsType);
        } else {
            boolean isCollectionOrArray = type.isArray() || Collection.class.isAssignableFrom(type);
            if (SIMPLE_TYPES.contains(type) || isCollectionOrArray || hasRequestParam || type.equals(MultipartFile.class)) {
                endpoint.addScalarQueryParam(paramName, tsType);
            } else {
                endpoint.setObjectQueryParamName(paramName);
                endpoint.setObjectQueryParamType(tsType);
            }
        }
    }

    private static String collectType(Type type) {
        if (type == null) return "any";
        if (type instanceof WildcardType) return "any";

        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (clazz == Object.class) return "any";
            if (clazz.equals(MultipartFile.class)) return "File | Blob";
            if (SIMPLE_TYPES.contains(clazz)) return mapSimpleType(clazz);
            if (clazz.isArray()) return collectType(clazz.getComponentType()) + "[]";
            if (Collection.class.isAssignableFrom(clazz)) return "any[]";

            // 处理原生类型（没有写泛型的类，如 Result）
            String tsName = parseComplexClass(clazz);
            TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();
            if (typeParameters.length > 0) {
                String anyArgs = Arrays.stream(typeParameters).map(t -> "any").collect(Collectors.joining(", "));
                return tsName + "<" + anyArgs + ">";
            }
            return tsName;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) pt.getRawType();
            Type[] actualArgs = pt.getActualTypeArguments();

            if (Collection.class.isAssignableFrom(rawType)) {
                return collectType(actualArgs[0]) + "[]";
            }

            String tsRawName = parseComplexClass(rawType);
            // 修复点：递归处理泛型参数，如果参数为空则填充 any
            String argsStr = Arrays.stream(actualArgs)
                    .map(AxiosGenerator::collectType)
                    .collect(Collectors.joining(", "));

            // 如果实际参数少于类定义的参数（极端发癫情况），补齐 any
            int expectedCount = rawType.getTypeParameters().length;
            if (actualArgs.length < expectedCount) {
                String extraAny = Collections.nCopies(expectedCount - actualArgs.length, "any").stream().collect(Collectors.joining(", "));
                argsStr = argsStr.isEmpty() ? extraAny : argsStr + ", " + extraAny;
            }

            return tsRawName + "<" + (argsStr.isEmpty() ? "any" : argsStr) + ">";
        }
        return "any";
    }

    private static String getUniqueTsName(Class<?> clazz) {
        String fullPath = clazz.getName();
        if (classNameMap.containsKey(fullPath)) return classNameMap.get(fullPath);

        String shortName = clazz.getSimpleName();
        boolean conflict = classNameMap.values().stream().anyMatch(v -> v.equals(shortName));

        String finalName = conflict ? fullPath.replace(".", "") : shortName;
        classNameMap.put(fullPath, finalName);
        return finalName;
    }

    private static String parseComplexClass(Class<?> clazz) {
        if (SIMPLE_TYPES.contains(clazz) || clazz.equals(Object.class)) return "any";

        String tsName = getUniqueTsName(clazz);
        if (typeDefinitions.containsKey(tsName)) return tsName;

        typeDefinitions.put(tsName, "");

        StringBuilder sb = new StringBuilder();
        sb.append("export interface ").append(tsName);
        String typeVars = Arrays.stream(clazz.getTypeParameters()).map(TypeVariable::getName).collect(Collectors.joining(", "));
        if (!typeVars.isEmpty()) sb.append("<").append(typeVars).append(">");

        sb.append(" {\n");
        for (Field f : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            sb.append("    ").append(f.getName()).append("?: ").append(getTsTypeFromField(f)).append(";\n");
        }
        sb.append("}\n");

        typeDefinitions.put(tsName, sb.toString());
        return tsName;
    }

    private static String getTsTypeFromField(Field f) {
        Type genericType = f.getGenericType();
        if (genericType instanceof TypeVariable) return genericType.getTypeName();
        return collectType(genericType);
    }

    private static String mapSimpleType(Class<?> clazz) {
        if (clazz == boolean.class || clazz == Boolean.class) return "boolean";
        if (clazz == void.class || clazz == Void.class) return "void";
        if (clazz.equals(MultipartFile.class)) return "File | Blob";
        return "string";
    }

    private static void generateGAxiosFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("/* eslint-disable */\nimport type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'\nimport axios from 'axios'\n");
        if (!typeDefinitions.isEmpty()) {
            sb.append(String.format("import type { %s } from './GType'\n\n", String.join(", ", typeDefinitions.keySet().stream().filter(k -> !k.equals("any")).collect(Collectors.toList()))));
        }
        sb.append("const instance: AxiosInstance = axios.create({ baseURL: import.meta.env.DEV ? '/api' : '/', timeout: 10000 })\n\n");
        sb.append("instance.interceptors.request.use((config: InternalAxiosRequestConfig) => { config.withCredentials = true; return config; }, (error) => Promise.reject(error));\n");
        sb.append("instance.interceptors.response.use((res: AxiosResponse) => res, (err) => Promise.reject(err));\n\n");
        sb.append("async function calling<T>(promise: Promise<AxiosResponse<T>>): Promise<T> {\n");
        sb.append("    try { const res = await promise; return res.data; } catch (error: any) { throw error; }\n}\n\n");

        groupedEndpoints.forEach((controllerName, list) -> {
            sb.append("// ############################################################\n");
            sb.append("// # > [RestController] ").append(controllerName).append("\n");
            sb.append("// ############################################################\n\n");

            for (Endpoint e : list) {
                List<String> funcArgs = new ArrayList<>();
                e.getPathParams().forEach((k, v) -> funcArgs.add(k + ": " + v));
                e.getScalarQueryParams().forEach((k, v) -> funcArgs.add(k + ": " + v));

                if (e.getBodyParamName() != null) {
                    funcArgs.add(String.format("%s: %s", e.getBodyParamName(), e.getBodyParamType()));
                }
                if (e.getObjectQueryParamName() != null) {
                    funcArgs.add(String.format("%s: %s = {} as %s", e.getObjectQueryParamName(), e.getObjectQueryParamType(), e.getObjectQueryParamType()));
                }

                String tsPath = e.fullPath.contains("{") ? "`" + e.fullPath.replace("{", "${") + "`" : "'" + e.fullPath + "'";
                sb.append(String.format("export function %s(%s): Promise<%s> {\n", e.methodName, String.join(", ", funcArgs), e.returnType));

                boolean hasFile = e.getScalarQueryParams().values().stream().anyMatch(v -> v.contains("File") || v.contains("Blob"));

                if (hasFile) {
                    sb.append("    const formData = new FormData();\n");
                    e.getScalarQueryParams().forEach((k, v) -> {
                        sb.append(String.format("    if (%s !== undefined) formData.append('%s', %s as any);\n", k, k, k));
                    });
                    if (e.getBodyParamName() != null) {
                        sb.append(String.format("    formData.append('%s', %s as any);\n", e.getBodyParamName(), e.getBodyParamName()));
                    }
                    String method = e.httpMethod.toLowerCase();
                    sb.append(String.format("    return calling<%s>(instance.%s(%s, formData))\n", e.returnType, method, tsPath));
                } else {
                    StringBuilder paramsPart = new StringBuilder("{ ");
                    e.getScalarQueryParams().keySet().forEach(k -> paramsPart.append(k).append(", "));
                    if (e.getObjectQueryParamName() != null) {
                        paramsPart.append("...").append(e.getObjectQueryParamName());
                    }
                    paramsPart.append(" }");

                    String method = e.httpMethod.toLowerCase();
                    String bodyData = (e.getBodyParamName() != null) ? e.getBodyParamName() : "null";

                    if ("get".equals(method) || "delete".equals(method)) {
                        sb.append(String.format("    return calling<%s>(instance.%s(%s, { params: %s, data: %s }))\n", e.returnType, method, tsPath, paramsPart.toString(), bodyData));
                    } else {
                        sb.append(String.format("    return calling<%s>(instance.%s(%s, %s, { params: %s }))\n", e.returnType, method, tsPath, bodyData, paramsPart.toString()));
                    }
                }
                sb.append("}\n\n");
            }
        });
        writeFile("GAxios.ts", sb.toString());
    }

    private static void generateGTypeFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("/* eslint-disable */\n/**\n * 自动生成\n */\n\n");
        typeDefinitions.values().stream()
                .filter(v -> v != null && !v.isEmpty())
                .forEach(def -> sb.append(def).append("\n"));
        writeFile("GType.ts", sb.toString());
    }

    private static void writeFile(String name, String content) {
        try (FileWriter w = new FileWriter("./" + name)) {
            w.write(content);
            System.out.println("[Success] " + name + " generated.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getHttpMethod(Method m) {
        if (m.isAnnotationPresent(GetMapping.class)) return "GET";
        if (m.isAnnotationPresent(PostMapping.class)) return "POST";
        if (m.isAnnotationPresent(PutMapping.class)) return "PUT";
        if (m.isAnnotationPresent(DeleteMapping.class)) return "DELETE";
        if (m.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping rm = m.getAnnotation(RequestMapping.class);
            if (rm.method().length > 0) return rm.method()[0].name();
        }
        return "GET";
    }

    private static String[] getPaths(Method m) {
        if (m.isAnnotationPresent(GetMapping.class)) return m.getAnnotation(GetMapping.class).value();
        if (m.isAnnotationPresent(PostMapping.class)) return m.getAnnotation(PostMapping.class).value();
        if (m.isAnnotationPresent(PutMapping.class)) return m.getAnnotation(PutMapping.class).value();
        if (m.isAnnotationPresent(DeleteMapping.class)) return m.getAnnotation(DeleteMapping.class).value();
        if (m.isAnnotationPresent(RequestMapping.class)) return m.getAnnotation(RequestMapping.class).value();
        return null;
    }
}

@Data
class Endpoint {
    String fullPath;
    String httpMethod;
    String methodName;
    String returnType;
    Map<String, String> pathParams = new LinkedHashMap<>();
    Map<String, String> scalarQueryParams = new LinkedHashMap<>();
    String objectQueryParamName;
    String objectQueryParamType;
    String bodyParamName;
    String bodyParamType;

    public void addPathParam(String n, String t) {
        pathParams.put(n, t);
    }

    public void addScalarQueryParam(String n, String t) {
        scalarQueryParams.put(n, t);
    }
}