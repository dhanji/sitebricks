/*
 * Copied from Gentyref project http://code.google.com/p/gentyref/
 * Reformatted and moved to fit package structure
 */
package com.google.sitebricks.conversion.generics;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;

class CaptureTypeImpl implements CaptureType
{
	private final WildcardType wildcard;
	private final TypeVariable<?> variable;
	private final Type[] lowerBounds;
	private Type[] upperBounds;

	/**
	 * Creates an uninitialized CaptureTypeImpl. Before using this type,
	 * {@link #init(VarMap)} must be called.
	 *
	 * @param wildcard
	 *            The wildcard this is a capture of
	 * @param variable
	 *            The type variable where the wildcard is a parameter for.
	 */
	public CaptureTypeImpl(WildcardType wildcard, TypeVariable<?> variable)
	{
		this.wildcard = wildcard;
		this.variable = variable;
		this.lowerBounds = wildcard.getLowerBounds();
	}

	/**
	 * Initialize this CaptureTypeImpl. This is needed for type variable bounds
	 * referring to each other: we need the capture of the argument.
	 */
	void init(VarMap varMap)
	{
		ArrayList<Type> upperBoundsList = new ArrayList<Type>();
		upperBoundsList.addAll(Arrays.asList(varMap.map(variable.getBounds())));
		upperBoundsList.addAll(Arrays.asList(wildcard.getUpperBounds()));
		upperBounds = new Type[upperBoundsList.size()];
		upperBoundsList.toArray(upperBounds);
	}

	/*
	 * @see com.googlecode.gentyref.CaptureType#getLowerBounds()
	 */
	public Type[] getLowerBounds()
	{
		return lowerBounds.clone();
	}

	/*
	 * @see com.googlecode.gentyref.CaptureType#getUpperBounds()
	 */
	public Type[] getUpperBounds()
	{
		assert upperBounds != null;
		return upperBounds.clone();
	}

	@Override
	public String toString()
	{
		return "capture of " + wildcard;
	}
}
