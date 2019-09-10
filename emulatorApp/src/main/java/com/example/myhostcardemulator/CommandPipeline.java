package com.example.myhostcardemulator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
* Class specifying methods and variables for every pipeline method category
* Each method category should extend this class and add its category-specific methods
 */
class MethodCategory {
    protected int catNum;
    protected Method[] methodArr;
    protected String catName;
    private static MethodCategory category_instance = new MethodCategory();

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
 * Class representing the result of the prior pipeline command
 */
class PipeParams{
    private boolean doContinue;
    private String currentString;

    public PipeParams(String command){
        doContinue = true;
        currentString = command;
    }

    public boolean getDoContinue() {
        return doContinue;
    }

    public void setDoContinue(boolean doContinue) {
        this.doContinue = doContinue;
    }

    public String getCurrentString() {
        return currentString;
    }

    public void setCurrentString(String result) {
        this.currentString = result;
    }
}

/**
* A class holding all method categories
* To set up a pipeline one should get methods by using this class' getMethod() method
 */
class CategoryList {

    static MethodCategory[] categoryArray;
    static CategoryList singleCategoryList = null;

    private CategoryList() throws NoSuchMethodException {
        categoryArray = new MethodCategory[] {new ComparisonCategory(), new ReplacementCategory()};
    }

    /**
     * Singleton instance getter
     * @return instance of the single CategoryList
     * @throws NoSuchMethodException
     */
    public CategoryList getInstance() throws NoSuchMethodException {
        if(singleCategoryList == null)
            singleCategoryList = new CategoryList();
        return singleCategoryList;
    }

    /**
     * Method for retrieving a method object for the pipeline
     * @param category Number of the method's category
     * @param method Number of the method inside the category
     * @return The specified method
     */
    public Method retrieveMethod(int category, int method){
        Method myMethod = categoryArray[category].getMethodArr()[method];
        return myMethod;
    }

    /**
     * Category holding all functions which compare a value with another and return a boolean
     */
    private class ComparisonCategory extends MethodCategory {
        /**
         * Constructor setting up the category's number, name and method array
         */
        private ComparisonCategory() throws NoSuchMethodException {
            catName = "Comparison Category";
            setupMethodArr(new MethodSpecs("stringEqual", new Class[] {PipeParams.class, String.class}), new MethodSpecs("stringStartsWith", new Class[] {PipeParams.class, String.class}));
        }

        /**
         * Compares two strings
         * @param params Result of prior pipeline command
         * @param comparison Second string
         * @return True if equal, False otherwise
         */
        public void stringEqual(PipeParams params, String comparison) {
            Boolean result = params.getCurrentString().equals(comparison);
            params.setDoContinue(result);
        }

        /**
         * Compares two strings
         * @param params Result of prior pipeline command
         * @param comparison Second string
         * @return True if first string starts with second string, False otherwise
         */
        public void stringStartsWith(PipeParams params, String comparison) {
            Boolean result = params.getCurrentString().startsWith(comparison);
            params.setDoContinue(result);
        }
    }


    private class ReplacementCategory extends MethodCategory{
        /**
         * Constructor setting up the category's number, name and method array
         */
        private ReplacementCategory() throws NoSuchMethodException {
            catName = "Replacement Category";
            setupMethodArr(new MethodSpecs("replaceStringWhole", new Class[] {PipeParams.class, String.class}), new MethodSpecs("replaceStringRange", new Class[] {PipeParams.class, String.class, int.class, int.class}));
        }

        /**
         * Entirely replace current string with new one
         * @param params Result of prior pipeline command
         * @param newString New string to replace the current string with
         */
        public void replaceStringWhole(PipeParams params, String newString){
            params.setCurrentString(newString);
        }

        /**
         * Replace part of the current string with a new one
         * @param params Result of prior pipeline command
         * @param newString New string to replace part of the current one with
         * @param start Start index of replacement
         * @param end End index of replacement
         */
        public void replaceStringRange(PipeParams params, String newString, int start, int end){
            start = Math.min(newString.length(), start);
            end = Math.min(newString.length(), end);
            int length = end - start;
            String result = params.getCurrentString().substring(start, end) + newString.substring(0, length);
            params.setCurrentString(result);
        }
    }
}

class SinglePipeStep{
    private Method stepMethod;
    private PipeParams pipeParams;
    private Object[] params;
    private Object parentObject;

    /**
     * Single Pipe Step constructor
     * @param method Method to be called on step
     * @param params Params to be passed on to method
     */
    public SinglePipeStep(Method method, PipeParams pipeParams, Object[] params, Object parentObject){
        stepMethod = method;
        this.parentObject = parentObject;
        this.pipeParams = pipeParams;

        this.params = new Object[params.length + 1];
        this.params[0] = pipeParams;
        for(int i = 1; i < this.params.length; i++){
            this.params[i] = params[i - 1];
        }
    }

    /**
     * Invoke step's method with step's params on step's parent object
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void doStep() throws InvocationTargetException, IllegalAccessException {
        if(pipeParams.getDoContinue())
            stepMethod.invoke(parentObject, params);
    }
}

public class CommandPipeline {
    private PipeParams currentPipeParams;
    private SinglePipeStep[] fullPipeline;

    /*public CreateCommandPipeline(){

    }*/

    /**
     * Performs all steps of pipeline
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void performPipeline() throws InvocationTargetException, IllegalAccessException {
        for(SinglePipeStep step : fullPipeline){
            step.doStep();
        }
    }
}
