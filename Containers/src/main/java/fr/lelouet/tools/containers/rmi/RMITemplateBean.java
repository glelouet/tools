package fr.lelouet.tools.containers.rmi;

import java.rmi.RemoteException;

import fr.lelouet.tools.containers.TemplateBean;

public class RMITemplateBean<E> implements TemplateBean<E> {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(RMITemplateBean.class);

	RemoteTemplateBean<E> target = null;

	public RMITemplateBean(RemoteTemplateBean<E> target) {
		this.target = target;
	}

	@Override
	public E get() {
		try {
			return target.get();
		} catch (RemoteException e) {
			logger.warn("", e);
			return null;
		}
	}

	@Override
	public void set(E e) {
		try {
			target.set(e);
		} catch (RemoteException e1) {
			logger.warn("", e1);
		}
	}
}
