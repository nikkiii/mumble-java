package org.nikkii.eventhub.event;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class EventHub {
	
	private static final String HANDLER_DESC = Type.getInternalName(EventExecutor.class);
	private static final String HANDLER_FUNC_DESC = Type.getMethodDescriptor(EventExecutor.class.getDeclaredMethods()[0]);
	
	private static final ASMClassLoader LOADER = new ASMClassLoader();
	
	private Map<Class<? extends Event>, List<EventExecutor>> handlers = new HashMap<Class<? extends Event>, List<EventExecutor>>();
	
	public void callEvent(Event event) {
		List<EventExecutor> list = handlers.get(event.getClass());
		if(list == null) {
			return;
		}
		for(EventExecutor listener : list) {
			try {
				listener.execute(event);
			} catch (EventException e) {
				e.printStackTrace();
			}
		}
	}

	public void registerListener(EventListener listener) {
		for(final Method method : listener.getClass().getMethods()) {
			final EventHandler eh = method.getAnnotation(EventHandler.class);
			if (eh == null) continue;
			final Class<?> checkClass;
			if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
				continue;
			}
			
			final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
			
			List<EventExecutor> list = handlers.get(eventClass);
			if(list == null) {
				list = new LinkedList<EventExecutor>();
				handlers.put(eventClass, list);
			}
			
			Class<?> execClass = createExecutor(method);
			
			try {
				EventExecutor executor = (EventExecutor) execClass.getConstructor(Object.class).newInstance(listener);
				
				list.add(executor);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Class<?> createExecutor(Method callback) {
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;
		
		String name = getUniqueName(callback);
		String desc = name.replace('.',  '/');
		String instType = Type.getInternalName(callback.getDeclaringClass());
		String eventType = Type.getInternalName(callback.getParameterTypes()[0]);		
		
		cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER, desc, null, "java/lang/Object", new String[]{ HANDLER_DESC });

		cw.visitSource(".dynamic", null);
		{
			cw.visitField(ACC_PUBLIC, "instance", "Ljava/lang/Object;", null, null).visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, desc, "instance", "Ljava/lang/Object;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "execute", HANDLER_FUNC_DESC, null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
			mv.visitTypeInsn(CHECKCAST, instType);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, eventType);
			mv.visitMethodInsn(INVOKEVIRTUAL, instType, callback.getName(), Type.getMethodDescriptor(callback));
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		cw.visitEnd();
		return LOADER.define(name, cw.toByteArray());
	}
	
	private static int id = 0;
	
	private String getUniqueName(Method callback) {
		return String.format("%s_%d_%s_%s_%s", getClass().getName(), id++, 
				callback.getDeclaringClass().getSimpleName(), 
				callback.getName(), 
				callback.getParameterTypes()[0].getSimpleName());
	}
	
	private static class ASMClassLoader extends ClassLoader {
		private ASMClassLoader() {
			super(ASMClassLoader.class.getClassLoader());
		}
		
		public Class<?> define(String name, byte[] data) {
			return defineClass(name, data, 0, data.length);
		}
	}
}
