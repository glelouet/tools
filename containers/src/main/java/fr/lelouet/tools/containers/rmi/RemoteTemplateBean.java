package fr.lelouet.tools.containers.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteTemplateBean<E> extends Remote {

	public E get() throws RemoteException;

	public void set(E e) throws RemoteException;

}
