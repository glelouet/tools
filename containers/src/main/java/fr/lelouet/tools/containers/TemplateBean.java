package fr.lelouet.tools.containers;

/** a simple bean for an typed object. get it or set it. */
public interface TemplateBean<E> {

	public E get();

	public void set(E e);

}
