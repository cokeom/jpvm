package org.jpvm.objects.types;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.jpvm.errors.PyException;
import org.jpvm.errors.PyUnsupportedOperator;
import org.jpvm.objects.*;
import org.jpvm.objects.annotation.PyClassMethod;
import org.jpvm.objects.pyinterface.TypeDescriptorGet;
import org.jpvm.objects.pyinterface.TypeDescriptorSet;
import org.jpvm.objects.pyinterface.TypeIterable;
import org.jpvm.pvm.MRO;
import org.jpvm.python.BuiltIn;

/** all subclass must override method getType {@link org.jpvm.objects.pyinterface.TypeCheck} */
public class PyTypeType extends PyObject {

  /* Objects behave like an unbound method */
  public static int Py_TPFLAGS_METHOD_DESCRIPTOR = (1 << 17);

  /* Objects support type attribute cache */

  public static int Py_TPFLAGS_HAVE_VERSION_TAG = (1 << 18);

  public static int Py_TPFLAGS_VALID_VERSION_TAG = (1 << 19);

  /* Type is abstract and cannot be instantiated */
  public static int Py_TPFLAGS_IS_ABSTRACT = (1 << 20);

  /* These flags are used to determine if a type is a subclass. */
  public static int Py_TPFLAGS_LONG_SUBCLASS = (1 << 24);
  public static int Py_TPFLAGS_LIST_SUBCLASS = (1 << 25);
  public static int Py_TPFLAGS_TUPLE_SUBCLASS = (1 << 26);
  public static int Py_TPFLAGS_BYTES_SUBCLASS = (1 << 27);
  public static int Py_TPFLAGS_UNICODE_SUBCLASS = (1 << 28);
  public static int Py_TPFLAGS_DICT_SUBCLASS = (1 << 29);
  public static int Py_TPFLAGS_BASE_EXC_SUBCLASS = (1 << 30);
  public static int Py_TPFLAGS_TYPE_SUBCLASS = (1 << 31);

  public static PyObject type = new PyTypeType(PyObject.class);

  protected List<PyObject> mro;
  protected PyTupleObject _mro;
  protected List<PyObject> bases;
  protected PyTupleObject _bases;

  /**
   * used in super to call clazz's method this is the {@code Class} of class object of this class
   * such as {@code clazz} in {@link PyLongType} is {@code PyLongObject.class}
   */
  protected Class<?> clazz;

  protected String name;
  private boolean typeReady;

  public PyTypeType(Class<?> clazz) {
    this.clazz = clazz;
    name = "type";
    // use List just to avoid ExceptionInInitializerError
    mro = new ArrayList<>();
    bases = new ArrayList<>();
    if (PyObject.type != null) bases.add(PyObject.type); // add base object to bases
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public static PyTupleObject ensureBaseObjectTypeInBases(PyTupleObject bases) {
    for (int i = 0; i < bases.size(); i++) {
      if (bases.get(i) == PyObject.type) return bases;
    }
    PyTupleObject res = new PyTupleObject(bases.size() + 1);
    for (int i = 0; i < bases.size(); i++) res.set(i, bases.get(i));
    res.set(bases.size(), PyObject.type);
    return res;
  }

  public void addBase(PyObject base) {
    for (PyObject basis : bases) {
      if (basis == base) return;
    }
    bases.add(base);
  }

  /** this object is subtype of r or not */
  public PyBoolObject isSubType(PyObject r) throws PyException {
    if (!typeReady) getMro();
    if (mro.stream().anyMatch(pyObject -> pyObject == r)) {
      return BuiltIn.True;
    }
    return BuiltIn.False;
  }

  /** lazy loading */
  public PyTupleObject getMro() throws PyException {
    if (!typeReady) {
      if (_mro != null) return _mro;
      typeReady = true;
      mro = MRO.mro(this);
      PyTupleObject object = new PyTupleObject(mro.size());
      for (int i = 0; i < mro.size(); i++) {
        object.set(i, mro.get(i));
      }
      _mro = object;
      return object;
    } else return _mro;
  }

  public void setMro(List<PyObject> mro) {
    this.mro = mro;
  }

  public List<PyObject> getBases() {
    return bases;
  }

  public void setBases(List<PyObject> bases) {
    this.bases = bases;
  }

  public PyTupleObject getBasesClass() {
    if (_bases != null) {
      return _bases;
    }
    PyTupleObject object = new PyTupleObject(bases.size());
    for (int i = 0; i < bases.size(); i++) {
      object.set(i, bases.get(i));
    }
    _bases = object;
    return object;
  }

  public void set_mro(PyTupleObject _mro) {
    this._mro = _mro;
  }

  public void set_bases(PyTupleObject _bases) {
    this._bases = _bases;
  }

  @Override
  public PyUnicodeObject getTypeName() {
    return PyUnicodeObject.getOrCreateFromInternStringPool(name, true);
  }

  @Override
  public PyObject getType() {
    return type;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public PyUnicodeObject str() {
    return new PyUnicodeObject(String.format("<class '%s'>", name));
  }

  @Override
  public PyUnicodeObject repr() {
    return str();
  }

  @Override
  public PyBoolObject richCompare(PyObject o, Operator op) throws PyUnsupportedOperator {
    if (Operator.Py_EQ == op) {
      if (this == o) return BuiltIn.True;
      else return BuiltIn.False;
    }
    throw new PyUnsupportedOperator(getTypeName() + " not support operator " + op);
  }

  @Override
  public PyObject call(PyObject self, PyTupleObject args, PyDictObject kwArgs) throws PyException {
    if (args.size() == 1) {
      PyObject name = args.get(0);
      String res = "<class '" + getType().getTypeName() + "' >";
      return new PyUnicodeObject(res);
    }
    if (args.size() < 3) throw new PyException(getTypeName() + " require at least 3 arguments");
    PyObject name = args.get(0);
    PyObject bases = args.get(1);
    PyObject dict = args.get(2);
    if (name == null)
      name = kwArgs.get(PyUnicodeObject.getOrCreateFromInternStringPool("name", true));
    if (bases == null)
      bases = kwArgs.get(PyUnicodeObject.getOrCreateFromInternStringPool("bases", true));
    if (dict == null)
      dict = kwArgs.get(PyUnicodeObject.getOrCreateFromInternStringPool("dict", true));
    if (name instanceof PyUnicodeObject n
        && bases instanceof TypeIterable
        && dict instanceof PyDictObject d) {
      PyTupleObject base = PyTupleObject.getTupleFromIterator(bases);
      PyPythonType res = new PyPythonType(n, null, d);
      base = ensureBaseObjectTypeInBases(base);
      List<PyObject> bs = res.getBases();
      bs.clear(); // clean bs see `public PyTypeType(Class<?> clazz)`
      for (int i = 0; i < base.size(); i++) bs.add(base.get(i));
      return res;
    }
    throw new PyException(
        "type() requires 3 arguments: name str, tuple or list of base classes, dict of attributes");
  }

  @Override
  protected PyObject lookUpType(PyObject key) throws PyException {
    PyObject res;
    PyTupleObject mro = getMro();
    for (int i = 0; i < mro.size(); i++) {
      PyObject object = mro.get(i);
      if (object != PyTypeType.type && object != this) {
        res = object.getAttr(key);
        if (res != null) return res;
      }
    }
    return null;
  }

  @Override
  public PyObject getAttr(PyObject key) throws PyException {
    PyObject descr = lookUpType(key);
    if (descr instanceof TypeDescriptorGet get && descr instanceof TypeDescriptorSet)
      return get.descrGet(this, getType());
    PyObject object = null;
    if (dict != null) {
      object = dict.get(key);
    }
    var name = (PyUnicodeObject) key;
    if (object == null) {
      try {
        Method method = this.getClass().getMethod(name.getData(), PyObject.parameterTypes);
        if (method.isAnnotationPresent(PyClassMethod.class))
          object = new PyMethodObject(this, method, name.getData());
      } catch (NoSuchMethodException ignore) {
      }
    }
    if (object == null) {
      object = Utils.loadFiled(this, name);
    }
    if (object == null) {
      try {
        Method method = clazz.getMethod(name.getData(), PyObject.parameterTypes);
        if (method.isAnnotationPresent(PyClassMethod.class))
          object = new PyMethodObject(method, name.getData());
      } catch (NoSuchMethodException ignore) {
      }
    }
    // PyFunctionObject take priority over PyMethodObject
    if (descr instanceof PyFunctionObject func && object instanceof PyMethodObject) {
      return func;
    }
    if (object != null) return object;
    if (descr != null) {
      if (descr instanceof TypeDescriptorGet get) return get.descrGet(this, getType());
      return descr;
    }
    return null;
  }
}
