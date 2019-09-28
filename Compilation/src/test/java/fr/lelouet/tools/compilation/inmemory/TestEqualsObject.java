package fr.lelouet.tools.compilation.inmemory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;

public class TestEqualsObject {

	@Test
	public void testProduceObject() throws JClassAlreadyExistsException, ClassNotFoundException, IOException {

		// create public boolean my.package.Object#equals(java.lang.Object other){
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

}
