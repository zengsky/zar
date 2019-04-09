package com.zeng.zar.core;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import com.zeng.zar.annotation.MarkPoint;
import com.zeng.zar.exception.ClassFinderException;

public class Model implements Serializable{
    
    private static final long serialVersionUID = -3376098921179941174L;
    
    @MarkPoint
    public static <M extends Model> M find(Serializable id){
        return newInstance();
    }
    
    @MarkPoint
    public static <M extends Model> M load(Serializable id){
        try {
            Class<M> modelClass = Model.getModelClass();
            int modifiers = modelClass.getModifiers();
            if(Modifier.isPrivate(modifiers) || Modifier.isFinal(modifiers))
                throw new RuntimeException("can not proxy for a private or final class");
            return ModelProxy.newInstance(modelClass);
        } catch (Exception e){
            throw new RuntimeException("failed to new model instance", e);
        }
    }
    
    @MarkPoint
    @SuppressWarnings("unchecked")
    public static <M extends Model> M newInstance(){
        M m = null;
        try {
            Class<M> modelClass = getModelClass();
            Constructor<?> c = modelClass.getDeclaredConstructors()[0];
            if(!c.isAccessible())
                c.setAccessible(true);
            if(modelClass.isMemberClass() && !Modifier.isStatic(modelClass.getModifiers())){
                Class<?> enclosingClass = modelClass.getEnclosingClass();
                m = (M)c.newInstance(enclosingClass.newInstance());
            }
            m = (M) c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("failed to new model instance", e);
        }
        return m;
    }
    
    @MarkPoint
    public static <M extends Model> Class<M> getModelClass(){
        Class<M> modelClass = ClassFinder.findClass(Model.class);
        if(modelClass == null)
            throw new ClassFinderException("model class not found");
        return modelClass;
    }

}