package ru.akirakozov.sd.refactoring.aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import ru.akirakozov.sd.refactoring.aspect.CallTree.CallTreeView;

@Aspect
public class TreeProfile {
    private CallTreeView callTree = null;

    @Around("@annotation(ru.akirakozov.sd.refactoring.aspect.Profile) && execution(* *(..))")
    public Object logMethodCall(final ProceedingJoinPoint jp) throws Throwable {
        if (callTree == null) {
            callTree = CallTree.startCallTree(jp.toString());
        } else {
            callTree.enterCall(jp.toString());
        }

        Object result = jp.proceed();

        if (callTree.isRootCall()) {
            callTree.dumpTree();
            callTree = null;
        } else {
            callTree.exitCall();
        }
        return result;
    }
}
