package com.zeng.zar.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zeng.zar.exception.ClassFinderException;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LineNumberAttribute;

public class ClassFinder {
    
    private static ThreadLocal<Integer> index = new ThreadLocal<Integer>();
    private static ThreadLocal<FinderCache> cacheLocal = new ThreadLocal<FinderCache>();
    private static ClassPool classPool = new ClassPool(true);
    
    @SuppressWarnings("unchecked")
    public static <T> Class<T> findClass(Class<?> classType){
        Class<T> clazz = null;
        FinderData fdata = new FinderData(new Throwable(), classType);
        FinderCache cache = getCache();
        try {
            if(!cache.containsKey(fdata.key)){
                if(cache.size() > 0){
                    cache.clear();
                }
                initCache(fdata);
            }
            List<String> list = cache.get(fdata.key);
            if(list != null && list.size() > 0){
                String className = cache.getEntry(fdata.key);
                clazz = (Class<T>) Class.forName(className);
            }else{
                throw new Exception(fdata.key + " not found in cache");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassFinderException("failed to find class", e);
        } finally {
            resetIndex();
        }
        
        return clazz;
    }
    
    private static void initCache(FinderData fdata) throws NotFoundException, BadBytecode{
        CtMethod[] methods = fdata.targetClass.getDeclaredMethods(fdata.methodName);
        
        int start=0, end = 0;
        CodeAttribute code = null;
        
        o_o:for(CtMethod method : methods){
            code = method.getMethodInfo().getCodeAttribute();
            LineNumberAttribute line = (LineNumberAttribute) code.getAttribute(LineNumberAttribute.tag);
            int length = line.tableLength();
            start = line.lineNumber(0);
            end = line.lineNumber(length-1);
            if(start > fdata.lineNumber || end < fdata.lineNumber)
                continue;
            for(int i=0; i<length; i++){
                if(fdata.lineNumber != line.lineNumber(i))
                    continue;
                start = line.startPc(i);
                end = i<length-1 ? line.startPc(i+1):start;
                break o_o;
            }
        }
        
        if(code == null)
            throw new ClassFinderException("method code attribute is null");
        CodeIterator it = code.iterator();
        ConstPool cp = code.getConstPool();
        //TODO lambda method
        if(start == end && end == 0)
            end = it.getCodeLength() - 1;
        while(it.hasNext()){
            int n = it.next();
            if(start > n || n > end)
                continue;
            String str = InstructionPrinter.instructionString(it, n, cp);
            Matcher m = fdata.pattern.matcher(str);
            if(m.find()){
                getCache().addCache(fdata.key, m.group(1));
            }
        }
    }
    
    private static void initIndex(Class<?> clazz, StackTraceElement[] elems) {
        if(clazz != null) {
            for(StackTraceElement elem : elems) {
                if(elem.getClassName().equals(clazz.getCanonicalName())) {
                    increaseIndex();
                }
            }
        }else {
            throw new ClassFinderException("target class is null");
        }
    }
    
    private static FinderCache getCache(){
        if(cacheLocal.get() == null)
            cacheLocal.set(new FinderCache());
        return cacheLocal.get();
    }
    
    private static void increaseIndex(){
        if(index.get() == null)
            index.set(1);
        else{
            index.set(index.get() + 1);
        }
    }
    
    private static void resetIndex(){
        index.set(0);
    }
    
    /**
     * finder metedata class
     */
    private static class FinderData{
        
        Pattern pattern;
        String key;
        String className;
        String methodName;
        Integer lineNumber;
        String classType;
        
        CtClass targetClass;
        
        public FinderData(Throwable throwable, Class<?> clazz){
            StackTraceElement[] elems = throwable.getStackTrace();
            //initIndex(elems);
            initIndex(clazz, elems);
            classType = clazz.getName().replace(".", "/");
            setTargetInfo(elems[index.get() + 1]);
            setPattern(elems[index.get()]);
            initTargetClass();
        }
        
        public void setKey(StackTraceElement target){
            this.key = String.format("%s_%s_%s", target.getClassName(), target.getMethodName(), target.getLineNumber());
        }
        
        public void setPattern(StackTraceElement source){
            String pattrnStr = String.format("Method (.+).%s.+\\)L%s;\\)", source.getMethodName(), classType);
            this.pattern = Pattern.compile(pattrnStr);
        }
        
        public void setTargetInfo(StackTraceElement target){
            this.className = target.getClassName();
            this.methodName = target.getMethodName();
            this.lineNumber = target.getLineNumber();
            this.setKey(target);
        }
        
        public void initTargetClass(){
             try {
                 targetClass = classPool.get(this.className);
            } catch (NotFoundException e) {
                throw new ClassFinderException("failed to init target class", e);
            }
        }
    }
    
    /**
     * finder cache class
     * @param <K>
     */
    private static class FinderCache extends HashMap<Object,List<String>>{
        
        private static final long serialVersionUID = -7122865934933403512L;
        
        private Map<Object,Integer> countMap = new HashMap<Object,Integer>();
        
        public void addCache(Object key, String value){
            List<String> list = null;
            if(containsKey(key)){
                list = get(key);
            }else{
                list = new ArrayList<String>();
            }
            list.add(value);
            put(key, list);
        }
        
        public String getEntry(Object key){
            List<String> v = get(key);
            if(v == null)
                throw new RuntimeException("no value of key<"+ key +">" );
            Integer n = countMap.get(key);
            if(n == null || v.size() == n)
                n = 0;
            countMap.put(key, n+1);
            return v.get(n);
        }
        
    }
}

