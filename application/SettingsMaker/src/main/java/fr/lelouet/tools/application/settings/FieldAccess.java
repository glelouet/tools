package fr.lelouet.tools.application.settings;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JPackage;

import fr.lelouet.tools.application.settings.description.AccessDescription;

/** generate a bean for given type in a {@link JDefinedClass} */
public interface FieldAccess {

	/**
	 * create a field in a class.
	 *
	 * @param clazz
	 *          the class to create the field into
	 * @param name
	 *          name of the field
	 * @param fieldType
	 *          type of the field
	 * @param description
	 *          description of the field
	 */
	void createField(JDefinedClass clazz, String name, AbstractJType fieldType, String description);

	void setParams(AccessDescription params);

	JDefinedClass makeRootClass(SettingsCompiler settingsCompiler);

	AbstractJClass listRef();

	AbstractJClass mapRef();

	JDefinedClass makeClass(JPackage jPackage, String makeJavaClassName) throws JClassAlreadyExistsException;
}
