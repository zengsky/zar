package com.zeng.zar.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.zeng.zar.model.User;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class ModelProxy {

    private static final String INVOKER_NAME = ModelLazyInvoker.class.getName();
    
    private static ClassPool classPool = new ClassPool(true);
    
    private static Integer index = 0x186A0;
    
    public static void main(String[] args) {
        try {
            for(int i=0; i<10; i++){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(newInstance(User.class));
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <M extends Model> M newInstance(Class<M> clazz){
        if(clazz == null){
            return null;
        }
        if(!isModelType(clazz)){
            throw new RuntimeException("not support a non-model class type");
        }
        CtClass proxyClass = null;
        try {
            proxyClass = getProxyClass(clazz);
            proxyClass.addField(getInvoker(proxyClass));
            CtMethod[] methods = proxyClass.getSuperclass().getDeclaredMethods();
            for(CtMethod method : methods){
                String name = method.getName();
                if(name.startsWith("get")){
                    CtMethod delegator = CtNewMethod.delegator(method, proxyClass);
                    delegator.insertBefore("this.lazyInvoker.invoke();");
                    proxyClass.addMethod(delegator);
                }
            }
            Class<?> proxy = proxyClass.toClass();
            Constructor<?> c = proxy.getDeclaredConstructors()[0];
            if(!c.isAccessible()){
                c.setAccessible(true);
            }
            if(clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())){
                Class<?> enclosingClass = clazz.getEnclosingClass();
                return (M)c.newInstance(enclosingClass.newInstance());
            }
            return (M) c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("failed to new proxy-model instance", e);
        } finally {
            if(proxyClass != null) {
                proxyClass.detach();
            }
        }
    }
    
    private static CtField getInvoker(CtClass proxyClass) throws CannotCompileException{
        StringBuilder field = new StringBuilder();
        field.append("private ");
        field.append(INVOKER_NAME);
        field.append(" lazyInvoker = new ");
        field.append(INVOKER_NAME);
        field.append("(this);");
        return CtField.make(field.toString(), proxyClass);
    }
    
    private static CtClass getProxyClass(Class<?> clazz) throws NotFoundException, CannotCompileException{
        CtClass targetClass = classPool.get(clazz.getName());
        StringBuffer className = new StringBuffer();
        className.append(clazz.getPackage().getName());
        className.append(".ModelProxy$$");
        className.append(clazz.getSimpleName());
        className.append("$$");
        className.append(getIndex());
        CtClass proxyClass = classPool.makeClass(className.toString());
        proxyClass.setSuperclass(targetClass);
        proxyClass.setInterfaces(targetClass.getInterfaces());
        return proxyClass;
    }
    
    private static String getIndex(){
        synchronized (index) {
            return Integer.toHexString(++index);
        }
    }
    
    private static <M extends Model> boolean isModelType(Class<M> clazz){
        return clazz.getSuperclass() == Model.class;
    }
    
    public static class ModelLazyInvoker{
        
        private Object proxyObject;
        
        public ModelLazyInvoker(Object proxyObject){
            this.proxyObject = proxyObject;
        }
        
        public void invoke() {
            Class<?> clazz = proxyObject.getClass();
            Method[] methods = clazz.getSuperclass().getDeclaredMethods();
            for(Method method : methods){
                if(method.getName().startsWith("set")){
                    System.out.println("-->" + method);
                    try {
                        method.invoke(proxyObject, getParamValues(method.getParameterTypes()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } 
                }
            }
        }
        
        //TODO
        public Object[] getParamValues(Class<?>[] paramTypes){
            Object[] values =  new Object[paramTypes.length];
            for(int i=0; i<values.length; i++){
                Class<?> clazz = paramTypes[i];
                if(clazz == String.class){
                    values[i] = "hello";
                }else if(clazz == Integer.class || clazz == int.class){
                    values[i] = 1;
                }
            }
            return values;
        }
        
    }
}
