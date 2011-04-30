package pt.ist.expenditureTrackingSystem.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class VirtualHostFilterCollectionWrapper<Type extends VirtualHostAware> implements Set<Type>, List<Type> {

    public class WrapperIterator implements Iterator<Type> {

	private final Iterator<Type> iterator;
	private Type next;

	public WrapperIterator(final Iterator<Type> iterator) {
	    this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
	    if (next != null) {
		return true;
	    }
	    while (iterator.hasNext()) {
		final Type type = iterator.next();
		if (type.isConnectedToCurrentHost()) {
		    next = type;
		    return true;
		}
	    }
	    return false;
	}

	@Override
	public Type next() {
	    if (next == null) {
		hasNext();
		return next;
	    } else {
		final Type type = next;
		next = null;
		return type;
	    }
	}

	@Override
	public void remove() {
	    iterator.remove();
	}
    }


    private final Collection<Type> wrapped;

    public VirtualHostFilterCollectionWrapper(Collection<Type> wrapped) {
	this.wrapped = wrapped;
    }

    @Override
    public boolean add(Type object) {
	return wrapped.add(object);
    }

    @Override
    public boolean addAll(Collection<? extends Type> objects) {
	return wrapped.addAll(objects);
    }

    @Override
    public void clear() {
	wrapped.clear();
    }

    @Override
    public boolean contains(Object object) {
	return wrapped.contains(object) && ((VirtualHostAware) object).isConnectedToCurrentHost();
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
	for (Object object : objects) {
	    if (!wrapped.contains(object) || !((VirtualHostAware) object).isConnectedToCurrentHost()) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public boolean isEmpty() {
	for (final Type type : wrapped) {
	    if (type.isConnectedToCurrentHost()) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public Iterator<Type> iterator() {
	return new WrapperIterator(wrapped.iterator());
    }

    @Override
    public boolean remove(Object object) {
	return wrapped.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
	return wrapped.removeAll(objects);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
	return wrapped.retainAll(objects);
    }

    private List<Type> filter() {
	List<Type> filtered = new ArrayList<Type>();
	for (Type type : wrapped) {
	    if (type.isConnectedToCurrentHost()) {
		filtered.add(type);
	    }
	}
	return filtered;
    }

    @Override
    public int size() {
	int count = 0;
	for (final Type type : wrapped) {
	    if (type.isConnectedToCurrentHost()) {
		count++;
	    }
	}
	return count;
    }

    @Override
    public Object[] toArray() {
	return filter().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
	return filter().toArray(a);
    }

    @Override
    public void add(int index, Type element) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Type> c) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Type get(int index) {
	throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
	throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
	throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<Type> listIterator() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<Type> listIterator(int index) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Type remove(int index) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Type set(int index, Type element) {
	throw new UnsupportedOperationException();
    }

    @Override
    public List<Type> subList(int fromIndex, int toIndex) {
	throw new UnsupportedOperationException();
    }
}
