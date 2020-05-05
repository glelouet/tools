package fr.lelouet.tools.application.settings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JNarrowedClass;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.writer.JCMWriter;

import fr.lelouet.tools.application.settings.beanmakers.JavaBeans;
import fr.lelouet.tools.application.settings.beanmakers.Public;

public class SettingsCompiler {

	/**
	 *
	 * @param args
	 *          source targetfolder<br />
	 *          source can be either a file opened as a resource, or a class that
	 *          must implement {@link SettingsLoader}.<br />
	 *          targetfolder is default "." and must not exist as anything else
	 *          than a directory.
	 * @throws IOException
	 *
	 */
	@SuppressWarnings("unchecked")
	public static void main(String... args) throws IOException {
		String source = args[0];
		String targetFolder = ".";
		if (args.length > 1) {
			targetFolder = args[1];
		}
		File out = new File(targetFolder);
		delDir(out);
		out.mkdirs();
		// try to open a stream;
		InputStream input = SettingsCompiler.class.getClassLoader().getResourceAsStream(source);
		SettingsDescription settings = null;
		if (input == null) {
			try {
				Class<SettingsLoader> clazz = (Class<SettingsLoader>) SettingsCompiler.class.getClassLoader().loadClass(source);
				SettingsLoader loader = clazz.getConstructor().newInstance();
				settings = loader.load();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new UnsupportedOperationException("catch this", e);
			}
		} else {
			settings = SettingsDescription.load(input);
		}

		JCodeModel jcm = new SettingsCompiler(settings).generate();
		new JCMWriter(jcm).build(out, out, null);
	}

	public static void delDir(File delete) {
		if (delete.exists()) {
			if (delete.isDirectory()) {
				for (File child : delete.listFiles()) {
					delDir(child);
				}
			}
			delete.delete();
		}
	}

	protected SettingsDescription settings;

	public SettingsDescription settings() {
		return settings;
	}

	public SettingsCompiler(SettingsDescription settings) {
		this.settings = settings;
	}

	FieldAccess bean = null;

	JCodeModel jcm;

	public JCodeModel codeModel() {
		return jcm;
	}

	JPackage rootPck;

	public JPackage rootPackage() {
		return rootPck;
	}

	JDefinedClass rootClass;

	public JDefinedClass rootClass() {
		return rootClass;
	}

	JPackage typesPck;

	public JPackage typesPackage() {
		return typesPck;
	}

	public JCodeModel generate() {
		bean = loadBeanMaker();
		jcm = new JCodeModel();
		rootPck = jcm._package(settings.path.toLowerCase());
		rootClass = bean.makeRootClass(this);
		typesPck = rootPck.subPackage("types");
		addBasicTypes(knownTypes);
		if (settings.forceAllTypes) {
			for (String s : settings.types.keySet()) {
				getType(s);
			}
		}
		addTypeStruct(rootClass, settings.contains);
		return jcm;
	}

	@SuppressWarnings("unchecked")
	protected FieldAccess loadBeanMaker() {
		Class<? extends FieldAccess> clazz = Public.class;
		Map<String, Class<? extends FieldAccess>> knownMakers = new HashMap<>();
		knownMakers.put("public", clazz);
		knownMakers.put("javabeans", JavaBeans.class);
		if (settings.access != null) {
			if (knownMakers.containsKey(settings.access)) {
				clazz = knownMakers.get(settings.access);
			} else {
				try {
					clazz = (Class<? extends FieldAccess>) SettingsCompiler.class.getClassLoader().loadClass(settings.access);
				} catch (ClassNotFoundException e) {
					throw new UnsupportedOperationException("catch this", e);
				}
			}
		}
		try {
			return clazz.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
	}

	protected void addBasicTypes(Map<String, AbstractJType> knownTypes) {
		knownTypes.put("int", jcm.INT);
		knownTypes.put("double", jcm.DOUBLE);
		knownTypes.put("float", jcm.FLOAT);
		knownTypes.put("long", jcm.LONG);
		knownTypes.put("string", jcm.ref(String.class));
		knownTypes.put("bool", jcm.BOOLEAN);
		knownTypes.put("char", jcm.CHAR);
	}

	/**
	 * build a type, if required, and all the subtypes
	 *
	 * @param type
	 * @param pck
	 * @param name
	 * @param store
	 *          set to true if this is a ref, and therefore can be asked for
	 *          recursively.
	 * @return
	 */
	protected AbstractJType makeType(TypeDescription type, Supplier<JPackage> pck, String name, boolean store) {
		if (type.ref != null) {
			return getType(type.ref);
		}
		if (type.struct != null) {
			// make a structure
			try {
				JDefinedClass creation = bean.makeClass(pck.get(), makeJavaClassName(name));
				if (store) {
					knownTypes.put(name, creation);
				}
				addTypeStruct(creation, type.struct);
				return creation;
			} catch (JClassAlreadyExistsException e) {
				throw new UnsupportedOperationException("catch this", e);
			}
		}
		if (type.of != null) {
			JDefinedClass creation = null;
			if (store) {
				try {
					creation = bean.makeClass(pck.get(), makeJavaClassName(name));
				} catch (JClassAlreadyExistsException e) {
					throw new UnsupportedOperationException("catch this", e);
				}
				knownTypes.put(name, creation);
			}
			JNarrowedClass clazz = bean.listRef().narrow(makeType(type.of, pck, name, false));
			if (creation != null) {
				creation._extends(clazz);
			}
			return clazz;
		}
		if (type.key != null && type.val != null) {
			JDefinedClass creation = null;
			if (store) {
				try {
					creation = bean.makeClass(pck.get(), makeJavaClassName(name));
				} catch (JClassAlreadyExistsException e) {
					throw new UnsupportedOperationException("catch this", e);
				}
				knownTypes.put(name, creation);
			}
			JNarrowedClass clazz = bean.mapRef().narrow(makeType(type.key, pck, name, false))
					.narrow(makeType(type.val, pck, name, false));
			if (creation != null) {
				creation._extends(clazz);
			}
			return clazz;
		}
		throw new UnsupportedOperationException("can't handle type " + type);
	}

	/** add correct fields in the class */
	protected void addTypeStruct(JDefinedClass clazz, Map<String, TypeDescription> struct) {
		for (Entry<String, TypeDescription> e : struct.entrySet()) {
			TypeDescription td = e.getValue();
			bean.createField(clazz, e.getKey(),
					makeType(td, () -> clazz.getPackage().subPackage(clazz.name().toLowerCase()), e.getKey(), false), td.desc);
		}
	}

	private Map<String, AbstractJType> knownTypes = new HashMap<>();

	/**
	 * get a {@link JDefinedClass} from a type reference.
	 *
	 * @param typeName
	 *          the reference of the type
	 * @return
	 */
	protected AbstractJType getType(String typeName) {
		if (knownTypes.containsKey(typeName)) {
			return knownTypes.get(typeName);
		}
		TypeDescription td = settings.types.get(typeName);
		if (td == null) {
			throw new NullPointerException("can't find reference " + typeName);
		}
		return makeType(td, () -> typesPck, typeName, true);
	}

	/**
	 * make a correct java base name : replace all non-alphanum characters by
	 * underscore, then ensure
	 *
	 * @param baseName
	 * @return
	 */
	public static String makeJavaClassName(String baseName) {
		String goodchars = baseName.replaceAll("[^a-zA-Z0-9]+", "_").replaceAll("^_+", "");
		char firstChar = goodchars.charAt(0);
		if (firstChar <= '9' && firstChar >= 0) {
			return "C_" + goodchars;
		} else {
			return ("" + firstChar).toUpperCase() + goodchars.substring(1);
		}
	}


}
