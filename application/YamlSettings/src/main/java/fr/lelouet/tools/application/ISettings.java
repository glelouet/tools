package fr.lelouet.tools.application;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import fr.lelouet.tools.application.yaml.CleanRepresenter;
import fr.lelouet.tools.application.yaml.YAMLTools;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;


/**
 * data stored locally for any platform.
 * <p>
 * The data stored are this class fields. those fields must be primitive or
 * collections.
 * </p>
 *
 */
public interface ISettings {

	Logger logger = LoggerFactory.getLogger(ISettings.class);

	/**
	 *
	 * @return the name of the application, defaults to
	 *         this.getClass().getcannonicalName()
	 */
	default String getAppName() {
		return getClass().getCanonicalName();
	}

	/**
	 *
	 * @return the file used to store this
	 */
	File getStoreFile();

	/**
	 * store this settings locally, overriding previous stored settings
	 */
	void store();

	/**
	 * request to store this after a delay. This aims at reducing disk overhead.
	 */
	void storeLater();

	/**
	 * delete the file used to store this.
	 */
	default void erase() {
		File f = getStoreFile();
		if (f.exists()) {
			f.delete();
		}
	}

	/**
	 * attach listeners to the observable elements and collections that call
	 * {@link #storeLater()} on modification.<br />
	 * collections also trigger the attachment of such a listener
	 *
	 * @param sets
	 */
	static void attachStoreListeners(ISettings sets) {
		attachStoreListener(sets, sets::storeLater, "");
	}

	static void attachStoreListener(Object ob, Runnable store, String indent) {
		if (ob == null) {
			logger.warn("null pointer in the setting");
			return;
		}
		if (ob.getClass().isPrimitive()
				|| ob.getClass().isEnum()
				|| ob.getClass().getPackageName() != null && ob.getClass().getPackageName().startsWith("java.lang")) {
			logger.trace(indent + "skip class " + ob.getClass().getSimpleName() + " for " + ob);
			return;
		}
		logger.trace(indent + "attaching store listener on class " + ob.getClass().getSimpleName());
		if (ob == null || store == null) {
			return;
		}
		if (ob instanceof ObservableMap<?, ?>) {
			ObservableMap<?, ?> om = (ObservableMap<?, ?>) ob;
			om.addListener((MapChangeListener<Object, Object>) change -> {
				store.run();
				if (change.wasAdded()) {
					// we added a new key
					if (!change.wasRemoved()) {
						Object key = change.getKey();
						if (key instanceof Observable) {
							attachStoreListener(key, store, indent + " ");
						}
					}
					if (change.wasAdded()) {
						Object value = change.getValueAdded();
						if (value instanceof Observable) {
							attachStoreListener(value, store, indent + " ");
						}
					}
				}
			});
			for (Object v : om.values()) {
				attachStoreListener(v, store, indent + " ");
			}
		} else if (ob instanceof ObservableSet<?>) {
			ObservableSet<?> os = (ObservableSet<?>) ob;
			os.addListener((SetChangeListener<Object>) change -> {
				store.run();
				if (change.wasAdded()) {
					Object value = change.getElementAdded();
					if (value instanceof Observable) {
						attachStoreListener(value, store, indent + " ");
					}
				}
			});
			for (Object v : os) {
				attachStoreListener(v, store, indent + " ");
			}
		} else if (ob instanceof ObservableList<?>) {
			ObservableList<?> os = (ObservableList<?>) ob;
			os.addListener((ListChangeListener<Object>) change -> {
				store.run();
				if (change.wasAdded()) {
					for (Object value : change.getAddedSubList()) {
						if (value instanceof Observable) {
							attachStoreListener(value, store, indent + " ");
						}
					}
				}
			});
			for (Object v : os) {
				attachStoreListener(v, store, indent + " ");
			}
		} else if (ob instanceof Observable) {
			((Observable) ob).addListener(ev -> store.run());
		} else {
			// hook listeners on public methods that return observable
			for (Method m : ob.getClass().getMethods()) {
				Class<?> cl = m.getReturnType();
				if (Observable.class.isAssignableFrom(cl)) {
					try {
						logger.trace(indent + "on method " + m.getName());
						attachStoreListener(m.invoke(ob), store, indent + " ");
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new UnsupportedOperationException(e);
					}
				}
			}
			// hook listeners on all public non transient field
			for (Field f : ob.getClass().getFields()) {
				// do not store the transient fields nor static fields
				if ((f.getModifiers() & Modifier.TRANSIENT) == 0 && (f.getModifiers() & Modifier.STATIC) == 0) {
					try {
						logger.trace(indent + "on field " + ob.getClass().getSimpleName() + "." + f.getName());
						attachStoreListener(f.get(ob), store, indent + " ");
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new UnsupportedOperationException("catch this", e);
					}
				}
			}
		}
	}

	/**
	 * make yaml for dumping
	 *
	 * @return
	 */
	default Yaml makeDumpYaml() {
		Yaml ret = new Yaml(makeYamlConstructor(true), makeYamlRepresenter(true), makeYamlOptions(true));
		return ret;
	}

	default Yaml makeLoadYaml() {
		Yaml ret = new Yaml(makeYamlConstructor(false), makeYamlRepresenter(false), makeYamlOptions(false));
		return ret;
	}

	default Constructor makeYamlConstructor(boolean dump) {
		return new Constructor(getClass(), new LoaderOptions());
	}

	default Representer makeYamlRepresenter(boolean dump) {
		CleanRepresenter ret = new CleanRepresenter();
		if (dump) {
			// ret.getPropertyUtils().setBeanAccess(BeanAccess.FIELD);
		} else {
			ret.getPropertyUtils().setSkipMissingProperties(true);
		}
		return ret;
	}

	default DumperOptions makeYamlOptions(boolean dump) {
		return YAMLTools.blockDumper();
	}

}
