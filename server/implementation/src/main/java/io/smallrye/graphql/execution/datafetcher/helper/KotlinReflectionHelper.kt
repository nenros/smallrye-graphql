package io.smallrye.graphql.execution.datafetcher.helper

import io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg
import io.smallrye.graphql.execution.event.EventEmitter
import io.smallrye.graphql.schema.model.Operation
import io.smallrye.graphql.spi.ClassloadingService
import io.smallrye.graphql.spi.LookupService

import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions


/**
 * Help with reflection on an operation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
class KotlinReflectionHelper(operation: Operation, eventEmitter: EventEmitter) {
    private val lookupService = LookupService.get()
    private val classloadingService = ClassloadingService.get()
    private var operationClass: KClass<*> = classloadingService.loadClass(operation.className).kotlin
    private var method: KFunction<*> =lookupMethod(operationClass, operation)

//    private final Operation operation;
//    private final EventEmitter eventEmitter;
//    private final Class<?> operationClass;
//    private final Method method;
    private val  injectContextAt = -1
    

//    public <T> T invokePrivileged(Object... arguments) {
//        final ClassLoader tccl = Thread.currentThread().contextClassLoader
//        return invokePrivileged(tccl, arguments)
//    }
//
//    public <T> T invokePrivileged(final ClassLoader classLoader, Object... arguments) {
//
//        try {
//            return (T) AccessController
//                    .doPrivileged(new PrivilegedExceptionAction<Object> {
//                        @Override
//                        public Object run() throws Exception {
//                            ClassLoader originalTccl = Thread.currentThread()
//                                    .getContextClassLoader()
//                            Thread.currentThread().contextClassLoader = classLoader
//
//                            try {
//                                return invoke(arguments)
//                            } finally {
//                                Thread.currentThread().contextClassLoader = originalTccl
//                            }
//                        }
//                    })
//        } catch (PrivilegedActionException e) {
//            throw new RuntimeException(e.getCause())
//        }
//    }
//
    suspend fun <T> invoke(arguments: Array<out Any>):T? {
    
        if(!method.isSuspend) {
            throw IllegalStateException("${method.name} not coroutine method")
        }
    val operationInstance = lookupService.getInstance(operationClass.java)

    return method.callSuspend(operationInstance, arguments[0]) as T?
    }
    
//    public <T> T invoke(Object... arguments) throws Exception {
//        try {
//            Object operationInstance = lookupService.getInstance(operationClass)
//            eventEmitter.fireBeforeMethodInvoke(new InvokeInfo(operationInstance, method, arguments))
//            if (this.injectContextAt > -1) {
//                arguments = injectContext(arguments)
//            }
//            return (T) this.method.invoke(operationInstance, arguments)
//        } catch (InvocationTargetException ex) {
//            //Invoked method has thrown something, unwrap
//            Throwable throwable = ex.getCause()
//
//            if (throwable instanceof Error) {
//                throw (Error) throwable
//            } else if (throwable instanceof GraphQLException) {
//                throw (GraphQLException) throwable
//            } else if (throwable instanceof Exception) {
//                throw (Exception) throwable
//            } else {
//                throw msg.dataFetcherException(operation, throwable)
//            }
//        }
//    }

    private fun  lookupMethod( operationClass: KClass<*>,  operation: Operation) : KFunction<*> {
            return operationClass.functions
                .find { it.name == operation.methodName }
                ?: throw msg.dataFetcherException(
                    operation, 
                    NoSuchMethodException("Method: ${operation.methodName} in class ${operationClass.simpleName}")
                )
    }

//    private Class<?>[] getParameterClasses(Operation operation) {
//        if (operation.hasArguments()) {
//            List<Class<?>> cl = new LinkedList<>()
//            int cnt = 0
//            for (Field argument : operation.getArguments()) {
//                // If the argument is wrapped, load the wrapper
//                if (argument.hasWrapper()) {
//                    Class<?> clazz = classloadingService.loadClass(argument.getWrapper().getWrapperClassName())
//                    cl.add(clazz)
//                } else {
//                    Class<?> clazz = classloadingService.loadClass(argument.getReference().getClassName())
//                    cl.add(clazz)
//                    if (argument.getReference().getClassName().equals(Context.class.getName())) {
//                        this.injectContextAt = cnt
//                    }
//                }
//                cnt++
//            }
//            return cl.toArray(new Class[] {})
//        }
//        return null
//    }
//
//    private Object[] injectContext(Object[] arguments) {
//        ArrayList list = new ArrayList(Arrays.asList(arguments))
//        list.set(injectContextAt, SmallRyeContext.getContext())
//        return list.toArray()
//    }
}
