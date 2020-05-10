package smarshare.coreservice.read.service;


import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class AdminProxy implements InvocationHandler {


    private final Object object;
    private AccessManagementAPIService accessManagementAPIService;

    public AdminProxy(Object object, AccessManagementAPIService accessManagementAPIService) {
        this.object = object;
        this.accessManagementAPIService = accessManagementAPIService;
    }

    public static <T> T newInstance(T object, AccessManagementAPIService accessManagementAPIService) {
        return (T) Proxy.newProxyInstance( object.getClass().getClassLoader(), object.getClass().getInterfaces(), new AdminProxy( object, accessManagementAPIService ) );
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        log.info( "Inside Admin Proxy" );
        if (method.getName().equals( "getFilesAndFoldersByUserIdAndBucketName" )) {
            if (accessManagementAPIService.doesAccessExist( (int) args[0], String.valueOf( args[1] ), "read" ))
                return method.invoke( object, args );
        }
        return "No Access";
    }
}
