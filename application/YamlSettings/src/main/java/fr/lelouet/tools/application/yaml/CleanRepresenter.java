package fr.lelouet.tools.application.yaml;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * represents a java object into yaml while ignoring null fields, zero value,
 * empty
 * collections ; and removing useless !!class tag
 */
public class CleanRepresenter extends Representer {

	public CleanRepresenter(DumperOptions options) {
		super(options);
	}

	public CleanRepresenter() {
		this(YAMLTools.blockDumper());
	}

	protected static Set<Object> ZEROS = new HashSet<>(
			Arrays.asList(Integer.valueOf(0), Long.valueOf(0), Float.valueOf(0), Double.valueOf(0)));

	static final PropertyUtils propUtil = new PropertyUtils() {
		@Override
		protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) {
			return getPropertiesMap(type, bAccess).values().stream().sequential()
					.filter(prop -> prop.isReadable() && (isAllowReadOnlyProperties() || prop.isWritable()))
					.collect(Collectors.toCollection(LinkedHashSet::new));
		}
	};

	{
		setPropertyUtils(propUtil);
	}

	/**
	 * skip a field when it is set to null or to an empty collection
	 */
	@Override
	protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
			Tag customTag) {
		if (propertyValue == null || propertyValue instanceof Collection && ((Collection<?>) propertyValue).isEmpty()
				|| propertyValue instanceof Map && ((Map<?, ?>) propertyValue).isEmpty() || ZEROS.contains(propertyValue)) {
			return null;
		} else {
			NodeTuple ret = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
			if (ret.getValueNode() instanceof MappingNode) {
				MappingNode mn = (MappingNode) ret.getValueNode();
				if (mn.getValue().size() == 0) {
					return null;
				}
			}
			return ret;
		}
	}

	@Override
	protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
		// remove the !!class
		if (!classTags.containsKey(javaBean.getClass())) {
			addClassTag(javaBean.getClass(), Tag.MAP);
		}
		MappingNode ret = super.representJavaBean(properties, javaBean);
		return ret;
	}
}