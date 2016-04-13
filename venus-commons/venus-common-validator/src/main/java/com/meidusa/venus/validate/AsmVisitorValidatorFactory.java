package com.meidusa.venus.validate;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.meidusa.venus.validate.exception.ValidationException;
import com.meidusa.venus.validate.expression.PolicyExpression;
import com.meidusa.venus.validate.validator.AsmValidatorSupport;
import com.meidusa.venus.validate.validator.Validator;

public class AsmVisitorValidatorFactory {

    private AtomicInteger markClassName = new AtomicInteger();

    private final String SUPER_CLASS = getType(AsmValidatorSupport.class);

    private ASMClassLoader classLoader = new ASMClassLoader();

    private Map<Class<?>, Method> annotationMethod = new HashMap<Class<?>, Method>();

    /**
     * for write asm description
     * 
     * @param type
     * @return
     */
    private String getPrimitiveLetter(Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        } else if (Void.TYPE.equals(type)) {
            return "V";
        } else if (Boolean.TYPE.equals(type)) {
            return "Z";
        } else if (Character.TYPE.equals(type)) {
            return "C";
        } else if (Byte.TYPE.equals(type)) {
            return "B";
        } else if (Short.TYPE.equals(type)) {
            return "S";
        } else if (Float.TYPE.equals(type)) {
            return "F";
        } else if (Long.TYPE.equals(type)) {
            return "J";
        } else if (Double.TYPE.equals(type)) {
            return "D";
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    /**
     * for write asm description
     * 
     * @param type
     * @return
     */
    public String getType(Class<?> type) {
        if (type.isArray()) {
            return "[" + getDesc(type.getComponentType());
        } else {
            if (!type.isPrimitive()) {
                String clsName = type.getCanonicalName();

                if (type.isMemberClass()) {
                    int lastDot = clsName.lastIndexOf(".");
                    clsName = clsName.substring(0, lastDot) + "$" + clsName.substring(lastDot + 1);
                }
                return clsName.replaceAll("\\.", "/");
            } else {
                return getPrimitiveLetter(type);
            }
        }
    }

    /**
     * for write asm description
     * 
     * @param type
     * @return
     */
    public String getDesc(Class<?> type) {
        if (type.isPrimitive()) {
            return getPrimitiveLetter(type);
        } else if (type.isArray()) {
            return "[" + getDesc(type.getComponentType());
        } else {
            return "L" + getType(type) + ";";
        }
    }

    /**
     * construct description for specified method
     * 
     * @param returnType
     * @param paramType
     * @return
     */
    private String constructMethodDesc(Class<?> returnType, Class<?>... paramType) {
        StringBuilder methodDesc = new StringBuilder();
        methodDesc.append('(');
        for (int i = 0; i < paramType.length; i++) {
            methodDesc.append(getDesc(paramType[i]));
        }
        methodDesc.append(')');
        if (returnType == Void.class) {
            methodDesc.append("V");
        } else {
            methodDesc.append(getDesc(returnType));
        }
        return methodDesc.toString();
    }

    private String generateClassName(Class<?> clazz) {
        String className = clazz.getCanonicalName();
        className = className.replace('.', '_');
        className += "_validator_generated_" + markClassName.getAndIncrement();
        return className;
    }

    public Validator createAsmVistorValidator(String policy, Class<?> clazz, String fieldName) {
        Class<? extends AsmValidatorSupport> validatorClazz = null;
        validatorClazz = createAsmVistorValidatorClassInternal(policy, clazz);
        AsmValidatorSupport asmValidator = null;
        try {
            asmValidator = validatorClazz.newInstance();
            asmValidator.setFieldName(fieldName);
            return asmValidator;
        } catch (Exception e) {
            throw new RuntimeException("cannot init validator ", e);
        }

    }

    private Class<? extends AsmValidatorSupport> createAsmVistorValidatorClassInternal(String policy, Class<?> clazz) {

        ClassWriter clsWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        AsmValidatorContext context = buildContext(policy, clazz);

        clsWriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, context.getValidatorClassName(), "null", SUPER_CLASS, null);
        writeField(clsWriter, context);
        writeInit(clsWriter, context);
        writeValidate(clsWriter, context);

        byte[] code = clsWriter.toByteArray();

        // try {
        // org.apache.commons.io.IOUtils.write(code,
        // new java.io.FileOutputStream("/opt/daisy/" +
        // context.getValidatorClassName() + ".class"));
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        Class<?> exampleClass = classLoader.defineClassPublic(context.getValidatorClassName(), code, 0, code.length);
        return (Class<? extends AsmValidatorSupport>) exampleClass;
    }

    private AsmValidatorContext buildContext(String policy, Class<?> clazz) {
        AsmValidatorContext context = new AsmValidatorContext();
        context.setVistorClass(clazz);
        context.setClassInfoList(generateClassInfos(policy, clazz));
        // context.setClassInfoList(new ArrayList<AsmValidatorClassInfo>());
        context.setFieldInfoList(generateFieldInfos(policy, clazz));
        context.setValidatorClassName(generateClassName(clazz));
        return context;
    }

    private List<AsmValidatorClassInfo> generateClassInfos(String policy, Class<?> clazz) {
        List<AsmValidatorClassInfo> classInfoList = new ArrayList<AsmValidatorClassInfo>();

        Class superClazz = clazz;
        while (superClazz != Object.class) {
            Annotation[] annotations = superClazz.getAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                Annotation validatorFactory = annotations[i].annotationType().getAnnotation(com.meidusa.venus.validate.validator.annotation.Validator.class);
                if (validatorFactory != null) {
                    AsmValidatorClassInfo classInfo = new AsmValidatorClassInfo();
                    classInfo.setAnnotionOrder(i);
                    classInfo.setClazz(superClazz);
                    classInfo.setValidatorFieldName(getClassValidatorName(classInfo));
                    String annotationPolicy = invokePolicy(annotations[i]);
                    if (PolicyExpression.match(policy, annotationPolicy)) {
                        classInfoList.add(classInfo);
                    }
                }
            }
            superClazz = superClazz.getSuperclass();
        }

        return classInfoList;
    }

    private String invokePolicy(Object obj) {
        try {
            Method method = annotationMethod.get(obj.getClass());
            if (method == null) {
                method = obj.getClass().getMethod("policy");
                annotationMethod.put(((Annotation) obj).annotationType(), method);
            }
            return (String) method.invoke(obj);

        } catch (Exception e) {
            // ignore here
            return "";
        }

    }

    private List<AsmValidatorFieldInfo> generateFieldInfos(String policy, Class<?> clazz) {
        // 鑾峰彇璇lass鍜屽叾鎵�湁superClass鍏充簬field鐨勬牎楠屼俊鎭�
        List<AsmValidatorFieldInfo> fieldInfoList = new ArrayList<AsmValidatorFieldInfo>();
        Class superClazz = clazz;
        while (superClazz != Object.class) {
            Field[] fields = superClazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                PropertyDescriptor pd;
                Method fieldGetMethod = null;
                try {
                    pd = new PropertyDescriptor(field.getName(), clazz);
                    fieldGetMethod = pd.getReadMethod();
                } catch (IntrospectionException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
                Annotation[] annotations = field.getAnnotations();
                for (int i = 0; i < annotations.length; i++) {
                    Annotation validatorFactory = annotations[i].annotationType()
                            .getAnnotation(com.meidusa.venus.validate.validator.annotation.Validator.class);

                    if (validatorFactory != null && fieldGetMethod != null) {
                        AsmValidatorFieldInfo info = new AsmValidatorFieldInfo();
                        info.setAnnotionOrder(i);
                        info.setField(field);
                        info.setGetMethod(fieldGetMethod);
                        info.setValidatorFieldName(getFieldValidatorName(info));

                        String annotationPolicy = invokePolicy(annotations[i]);
                        if (PolicyExpression.match(policy, annotationPolicy)) {
                            fieldInfoList.add(info);
                        }

                    }
                }
            }

            superClazz = superClazz.getSuperclass();
        }
        return fieldInfoList;

    }

    private void writeField(ClassWriter clsWriter, AsmValidatorContext context) {
        for (AsmValidatorClassInfo classInfo : context.getClassInfoList()) {
            clsWriter.visitField(Opcodes.ACC_PRIVATE, classInfo.getValidatorFieldName(), getDesc(Validator.class), "", null);
        }
        for (AsmValidatorFieldInfo fieldInfo : context.getFieldInfoList()) {
            clsWriter.visitField(Opcodes.ACC_PRIVATE, fieldInfo.getValidatorFieldName(), getDesc(Validator.class), "", null);
        }

    }

    private String getClassValidatorName(AsmValidatorClassInfo classInfo) {
        return classInfo.getClazz().getCanonicalName().replace('.', '_') + "_visitors_" + classInfo.getAnnotionOrder();
    }

    private String getFieldValidatorName(AsmValidatorFieldInfo fieldInfo) {
        return fieldInfo.getField().getName() + "_visitors_" + fieldInfo.getAnnotionOrder();
    }

    private void writeInit(ClassWriter clsWriter, AsmValidatorContext context) {

        MethodVisitor mw = clsWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", constructMethodDesc(Void.class, new Class<?>[0]), null, null);
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, getType(AsmValidatorSupport.class), "<init>", constructMethodDesc(Void.class, new Class<?>[0]));

        for (AsmValidatorClassInfo classInfo : context.getClassInfoList()) {
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitLdcInsn(org.objectweb.asm.Type.getType(classInfo.getClazz()));
            mw.visitIntInsn(Opcodes.BIPUSH, classInfo.getAnnotionOrder());
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, getType(AsmValidatorSupport.class), "createFromClass",
                    constructMethodDesc(Validator.class, Class.class, int.class));
            mw.visitFieldInsn(Opcodes.PUTFIELD, context.getValidatorClassName(), getClassValidatorName(classInfo), getDesc(Validator.class));
        }

        for (AsmValidatorFieldInfo fieldInfo : context.getFieldInfoList()) {
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitLdcInsn(org.objectweb.asm.Type.getType(fieldInfo.getField().getDeclaringClass()));
            mw.visitLdcInsn(fieldInfo.getField().getName());
            mw.visitIntInsn(Opcodes.BIPUSH, fieldInfo.getAnnotionOrder());
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, getType(AsmValidatorSupport.class), "createFromField",
                    constructMethodDesc(Validator.class, Class.class, String.class, int.class));
            mw.visitFieldInsn(Opcodes.PUTFIELD, context.getValidatorClassName(), getFieldValidatorName(fieldInfo), getDesc(Validator.class));
        }

        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(0, 0);
        mw.visitEnd();

    }

    private void writeValidate(ClassWriter clsWriter, AsmValidatorContext context) {

        MethodVisitor mw = clsWriter.visitMethod(Opcodes.ACC_PUBLIC, "validate", constructMethodDesc(Void.class, Object.class), null,
                new String[] { getType(ValidationException.class) });
        mw.visitVarInsn(Opcodes.ALOAD, 1);
        mw.visitTypeInsn(Opcodes.CHECKCAST, getType(context.getVistorClass()));
        mw.visitVarInsn(Opcodes.ASTORE, 2);
        for (AsmValidatorClassInfo classInfo : context.getClassInfoList()) {
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitFieldInsn(Opcodes.GETFIELD, context.getValidatorClassName(), classInfo.getValidatorFieldName(), getDesc(Validator.class));
            mw.visitVarInsn(Opcodes.ALOAD, 2);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, getType(Validator.class), "validate", constructMethodDesc(Void.class, Object.class));
        }

        for (AsmValidatorFieldInfo fieldInfo : context.getFieldInfoList()) {
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitFieldInsn(Opcodes.GETFIELD, context.getValidatorClassName(), fieldInfo.getValidatorFieldName(), getDesc(Validator.class));
            mw.visitVarInsn(Opcodes.ALOAD, 2);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, getType(context.getVistorClass()), fieldInfo.getGetMethod().getName(),
                    constructMethodDesc(fieldInfo.getGetMethod().getReturnType(), fieldInfo.getGetMethod().getParameterTypes()));

            if (fieldInfo.getGetMethod().getReturnType().isPrimitive()) {
                writePrimitive(mw, fieldInfo.getGetMethod().getReturnType());
            }
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, getType(Validator.class), "validate", constructMethodDesc(Void.class, Object.class));

        }

        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(0, 0);
        mw.visitEnd();
    }

    private void writePrimitive(MethodVisitor mw, Class<?> clazz) {
        if (clazz == int.class) {
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(Integer.class), "valueOf", constructMethodDesc(Integer.class, int.class));
        } else if (clazz == long.class) {
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(Long.class), "valueOf", constructMethodDesc(Long.class, long.class));
        } else if (clazz == double.class) {
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(Double.class), "valueOf", constructMethodDesc(Double.class, double.class));
        } else if (clazz == float.class) {
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(Float.class), "valueOf", constructMethodDesc(Float.class, float.class));
        } else if (clazz == boolean.class) {
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(Boolean.class), "valueOf", constructMethodDesc(Boolean.class, boolean.class));
        } else if (clazz == byte.class) {
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(Byte.class), "valueOf", constructMethodDesc(Byte.class, byte.class));
        } else if (clazz == char.class) {
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(Character.class), "valueOf", constructMethodDesc(Character.class, char.class));
        }

    }
}
