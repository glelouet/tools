package fr.lelouet.tools.compilation.inmemory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.testng.Assert;

import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;

public class TestJCodeModel {

	// ignored because this is a bug in jcodemodel
	// @org.testng.annotations.Test
	public void testProduceObject() throws JClassAlreadyExistsException, ClassNotFoundException, IOException {

		// create public boolean my.pckg.Object#equals(java.lang.Object other){
		// return true; }
		JCodeModel cm = new JCodeModel();
		JDefinedClass cl = cm._class(JMod.PUBLIC, "my.pckg.Object");
		JMethod mt = cl.method(JMod.PUBLIC, cm.BOOLEAN, "equals");
		mt.param(cm.ref(Object.class), "other");
		mt.body()._return(JExpr.TRUE);

		DynamicClassLoader dcl = DynamicClassLoader.generate(cm);
		Class<?> createdclass = dcl.findClass(cl.fullName());
		Method createdmethod = Stream.of(createdclass.getMethods()).filter(m -> m.getName().equals(mt.name())).findFirst()
				.get();
		Assert.assertEquals(createdmethod.getParameters()[0].getType(), Object.class);
	}

	// ignored because this is a bug in jcodemodel
	// @org.testng.annotations.Test
	public void testBadPackage() throws JClassAlreadyExistsException, ClassNotFoundException {
		JCodeModel cm = new JCodeModel();
		JDefinedClass myclass = cm._class(JMod.PUBLIC, "mypck.MyClass");
		JDefinedClass myObject = myclass._class(JMod.PUBLIC | JMod.STATIC, "Object");
		myclass.method(JMod.PUBLIC, cm.VOID, "call").param(Object.class, "obj");
		myclass.method(JMod.PUBLIC, cm.VOID, "call").param(myObject, "obj");

		DynamicClassLoader dcl = DynamicClassLoader.generate(cm);
		dcl.findClass(myclass.fullName());
	}

}
