package fr.lelouet.tools.application.settings.beanmakers;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JNarrowedClass;
import com.helger.jcodemodel.JVar;

/**
 * like {@link Public} but set the field to private, and generate get() and
 * set().
 *
 */
public class JavaBeans extends Public {

	@Override
	public void createField(JDefinedClass clazz, String name, AbstractJType fieldType) {
		JFieldVar f = clazz.field(JMod.PRIVATE, fieldType, name);
		if (!fieldType.isPrimitive()) {
			if (fieldType instanceof AbstractJClass && ((AbstractJClass) fieldType).isParameterized()) {
				AbstractJClass fieldclass = (AbstractJClass) fieldType;
				if (fieldclass instanceof JNarrowedClass) {
					fieldclass = ((JNarrowedClass) fieldclass).basis();
				}
				f.init(fieldclass.narrowEmpty()._new());
			} else {
				f.init(fieldType._new());
			}
		}
		String suffix = name.substring(0, 1).toUpperCase() + name.substring(1);
		// generate getter
		JMethod get = clazz.method(JMod.PUBLIC, fieldType, "get" + suffix);
		get.body()._return(f);
		// generate setter
		JMethod set = clazz.method(JMod.PUBLIC, clazz.owner().VOID, "set" + suffix);
		JVar param = set.param(fieldType, "value");
		set.body().assign(f, param);
	}
}
