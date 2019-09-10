package com.example.myhostcardemulator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
* Class specifying methods and variables for every pipeline method category
* Each method category should extend this class and add its category-specific methods
 */
class methodCategory {
    protected int catNum;
    protected Method[] methodArr;
    protected String catName;

    /**
    * This class holds a method's name and the types of its params for use with Class.getMethod() method
     */
    protected class MethodSpecs {
        private String name;
        private Class[] typeArray;

        /**
         * Method spec constructor
         * @param name The method's name
         * @param types The method's param types
         */
        public MethodSpecs(String name, Class[] types){
            this.name = name;
            this.typeArray = types;
        }

        /**
         * Name getter
         * @return The name of the method specified
         */
        public String getName() {
            return name;
        }

        /**
         * Type array getter
         * @return An array of the method's param types
         */
        public Class[] getTypeArray() {
            return typeArray;
        }
    }

    /**
     * @return This category's name
     */
    public String getCatName() {
        return catName;
    }

    /**
     * @return This category's method array
     */
    public Method[] getMethodArr() {
        return methodArr;
    }

    /**
     * @return This category's number
     */
    public int getCatNum() {
        return catNum;
    }

    /**
     * Method for setting up the category's method array
     * @param methods This is an array of methodSpecs each specifying a different function
     */
    protected void setupMethodArr(MethodSpecs ...methods) throws NoSuchMethodException {
        Method[] newMethodArr = new Method[methods.length];
        for(int i = 0; i < methods.length; i++){
            newMethodArr[i] = this.getClass().getMethod(methods[i].getName(), methods[i].getTypeArray());
        }
    }
}

/**
* A class holding all method categories
* To set up a pipeline one should get methods by using this class' getMethod() method
 */
class categoryList{

    /**
     * Category holding all functions which compare a value with another and return a boolean
     */
    private class comparisonCategory extends methodCategory{
        /**
         * Constructor setting up the category's number, name and method array
         */
        private comparisonCategory() throws NoSuchMethodException {
            catNum = 0;
            catName = "Comparison Category";
            setupMethodArr(new MethodSpecs("stringEqual", new Class[] {String.class, String.class}), new MethodSpecs("stringStartsWith", new Class[] {String.class, String.class}));
        }

        /**
         * Compares two strings
         * @param command First string
         * @param comparison Second string
         * @return True if equal, False otherwise
         */
        public boolean stringEqual(String command, String comparison) {
            return command.equals(comparison);
        }

        /**
         * Compares two strings
         * @param command First string
         * @param comparison Second string
         * @return True if first string starts with second string, False otherwise
         */
        public boolean stringStartsWith(String command, String comparison) {
            return command.startsWith(comparison);
        }
    }
}

public class CommandPipeline {
}
