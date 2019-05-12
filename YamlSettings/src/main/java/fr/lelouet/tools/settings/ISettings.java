package fr.lelouet.tools.settings;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import fr.lelouet.tools.settings.yaml.CleanRepresenter;
import fr.lelouet.tools.settings.yaml.YAMLTools;
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

	static final Logger logger = LoggerFactory.getLogger(ISettings.class);

	/**
	 *
	 * @return the name of the application, defaults to
	 *         this.getClass().getcannonicalName()
	 */
	public default String getAppName() {
		return getClass().getCanonicalName();
	}

	/**
	 *
	 * @return the file used to store this
	 */
	public File getStoreFile();

	/**
	 * store this settings locally, overriding previous stored settings
	 */
	public void store();

	/**
	 * request to store this after a delay. This aims at reducing disk overhead.
	 */
	public void storeLater();

	/**
	 * delete the file used to store this.
	 */
	public default void erase() {
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
	public static void attachStoreListeners(ISettings sets) {
		attachStoreListener(sets, sets::storeLater, "");
	}

	public static void attachStoreListener(Object ob, Runnable store, String indent) {
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
						if (key != null && key instanceof Observable) {
							attachStoreListener(key, store, indent + " ");
						}
					}
					if (change.wasAdded()) {
						Object value = change.getValueAdded();
						if (value != null && value instanceof Observable) {
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
					if (value != null && value instanceof Observable) {
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
						if (value != null && value instanceof Observable) {
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
					} catch (IllegalAccessException e) {
						throw new UnsupportedOperationException("catch this", e);
					} catch (IllegalArgumentException e) {
						throw new UnsupportedOperationException("catch this", e);
					} catch (InvocationTargetException e) {
						throw new UnsupportedOperationException("catch this", e);
					}
				}
			}
			// hook listeners on all public non transient field
			for (Field f : ob.getClass().getFields()) {
				// do not store the transient fields
				if ((f.getModifiers() & Modifier.TRANSIENT) == 0) {
					try {
						logger.trace(indent + "on field " + f.getName());
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
	public default Yaml makeDumpYaml() {
		Yaml ret = new Yaml(makeYamlConstructor(true), makeYamlRepresenter(true), makeYamlOptions(true));
		return ret;
	}

	public default Yaml makeLoadYaml() {
		Yaml ret = new Yaml(makeYamlConstructor(false), makeYamlRepresenter(false), makeYamlOptions(false));
		return ret;
	}

	public default Constructor makeYamlConstructor(boolean dump) {
		return new Constructor(getClass());
	}

	public default Representer makeYamlRepresenter(boolean dump) {
		CleanRepresenter ret = new CleanRepresenter();
		if (dump) {
			// ret.getPropertyUtils().setBeanAccess(BeanAccess.FIELD);
		} else {
			ret.getPropertyUtils().setSkipMissingProperties(true);
		}
		return ret;
	}

	public default DumperOptions makeYamlOptions(boolean dump) {
		return YAMLTools.blockDumper();
	}

}
