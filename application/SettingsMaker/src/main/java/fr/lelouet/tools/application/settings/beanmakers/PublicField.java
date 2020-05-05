package fr.lelouet.tools.application.settings.beanmakers;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JNarrowedClass;

import fr.lelouet.tools.application.settings.BeanMaker;
import fr.lelouet.tools.application.settings.SettingsCompiler;

public class PublicField implements BeanMaker {

	@Override
	public void createField(JDefinedClass clazz, String name, AbstractJType fieldType) {
		JFieldVar f = clazz.field(JMod.PUBLIC, fieldType, name);
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
	}

	@Override
	public JDefinedClass makeRootClass(SettingsCompiler settingsCompiler) {
		try {
			return settingsCompiler.rootPackage()
					._class(SettingsCompiler.makeJavaClassName(settingsCompiler.settings().name));
		} catch (JClassAlreadyExistsException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

}
