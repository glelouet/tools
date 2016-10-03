package fr.lelouet.tools.containers.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.lelouet.tools.containers.TemplateBean;

/** encapsulates a {@link TemplateBean} to provide a {@link RemoteTemplateBean} */
public class TemplateBeanInRMI<E> implements RemoteTemplateBean<E>, Remote {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(TemplateBeanInRMI.class);

	private TemplateBean<E> target;

	public TemplateBeanInRMI(TemplateBean<E> target) {
		super();
		this.target = target;
	}

	@Override
	public E get() throws RemoteException {
		return target.get();
	}

	@Override
	public void set(E e) throws RemoteException {
		target.set(e);
	}

}
