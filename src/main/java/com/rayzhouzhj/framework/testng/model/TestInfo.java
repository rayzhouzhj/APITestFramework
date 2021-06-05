package com.rayzhouzhj.framework.testng.model;

import com.rayzhouzhj.framework.annotations.*;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class TestInfo {
    private IInvokedMethod invokedMethod;
    private ITestResult testResult;
    private Method declaredMethod;

    public TestInfo(IInvokedMethod methodName, ITestResult testResult) {
        this.invokedMethod = methodName;
        this.testResult = testResult;

        this.declaredMethod = this.invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
    }

    public ITestResult getTestResult() {
        return this.testResult;
    }

    public IInvokedMethod getInvokedMethod() {
        return this.invokedMethod;
    }

    public Method getDeclaredMethod() {
        return this.declaredMethod;
    }

    public String getClassName() {
        return this.declaredMethod.getDeclaringClass().getSimpleName();
    }

    public String getClassDescription() {
        ClassDescription description = this.declaredMethod.getDeclaringClass().getAnnotation(ClassDescription.class);
        return description == null ? "" : description.value();
    }

    public String getMethodName() {
        return this.declaredMethod.getName();
    }

    public boolean isTestMethod() {
        return this.declaredMethod.getAnnotation(Test.class) != null;
    }

    public String[] getAuthorNames() {
        return declaredMethod.getAnnotation(Authors.class) == null ? null : declaredMethod.getAnnotation(Authors.class).name();
    }

    public String getTestName() {
        String dataProvider = null;
        Object dataParameter = this.invokedMethod.getTestResult().getParameters();
        if (((Object[]) dataParameter).length > 0) {
            dataProvider = (String) ((Object[]) dataParameter)[0];
        }

        return dataProvider == null ? this.declaredMethod.getName() : this.declaredMethod.getName() + " [" + dataProvider + "]";
    }

    public String getTestMethodDescription() {
        return this.declaredMethod.getAnnotation(Test.class).description();
    }

    public String[] getTestGroups() {
        return this.invokedMethod.getTestMethod().getGroups();
    }


}
