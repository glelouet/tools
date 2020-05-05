package fr.lelouet.tools.application.settings;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JPackage;

/** generate a bean for given type in a {@link JDefinedClass} */
public interface BeanMaker {

	/**
	 * create a field in a class.
	 *
	 * @param clazz
	 * @param name
	 * @param fieldType
	 */
	void createField(JDefinedClass clazz, String name, AbstractJType fieldType);

	JDefinedClass makeRootClass(SettingsCompiler settingsCompiler);

	AbstractJClass listRef();

	AbstractJClass mapRef();

	JDefinedClass makeClass(JPackage jPackage, String makeJavaClassName) throws JClassAlreadyExistsException;
}
