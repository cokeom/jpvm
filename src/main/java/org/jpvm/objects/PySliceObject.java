package org.jpvm.objects;

import org.jpvm.errors.PyNotImplemented;
import org.jpvm.errors.PyUnsupportedOperator;
import org.jpvm.internal.NumberHelper;
import org.jpvm.objects.pyinterface.TypeDoIterate;
import org.jpvm.objects.pyinterface.TypeIterable;
import org.jpvm.objects.types.PySliceType;
import org.jpvm.python.BuiltIn;

public class PySliceObject extends PyObject {

  public static PyObject type = new PySliceType();

  private final PyObject start;
  private final PyObject end;
  private final PyObject step;

  public PySliceObject(PyObject start, PyObject end, PyObject step) {
    this.start = start;
    this.end = end;
    this.step = step;
  }

  @Override
  public PyObject getType() {
    return type;
  }

  @Override
  public PyUnicodeObject getTypeName() {
    return type.getTypeName();
  }

  @Override
  public PyUnicodeObject str() {
    return new PyUnicodeObject(String.format("slice(%s, %s, %s,)", start.repr(), end.repr(), step.repr()));
  }

  @Override
  public PyUnicodeObject repr() {
    return str();
  }

  @Override
  public PyBoolObject richCompare(PyObject o, Operator op) throws PyUnsupportedOperator {
    if (op == Operator.Py_EQ) {
      if (!(o instanceof PySliceObject slice)) {
        return BuiltIn.False;
      }
      if (start == slice.start && end == slice.end && step == slice.step)
        return BuiltIn.True;
      return BuiltIn.False;
    }
    throw new PyUnsupportedOperator("not support operator " + op);
  }

  @Override
  public PyBoolObject isHashable() {
    return new PyBoolObject(true);
  }

  public PyObject getStart() {
    return start;
  }

  public PyObject getEnd() {
    return end;
  }

  public PyObject getStep() {
    return step;
  }

  public PyListObject unpacked(PyObject o) {
    if (!((o instanceof TypeIterable) || o instanceof TypeDoIterate))
      return null;
    TypeDoIterate it;
    if (o instanceof TypeIterable iter) {
      try {
        it = iter.getIterator();
      } catch (PyNotImplemented e) {
        return null;
      }
    }
    else
      it = (TypeDoIterate) o;
    int b, e, s;
    Long l;
    if (start == BuiltIn.None)
      b = 0;
    else {
      l = NumberHelper.transformPyObject2Long(start);
      assert l != null;
      b = l.intValue();
    }
    if (end == BuiltIn.None)
      e = it.size();
    else {
      l = NumberHelper.transformPyObject2Long(end);
      assert l != null;
      e = l.intValue();
    }

    if (step == BuiltIn.None)
      s = 1;
    else {
      l = NumberHelper.transformPyObject2Long(step);
      assert l != null;
      s = l.intValue();
    }
    if (s == 0)
      return null;
    if (e > it.size())
      e = it.size();
    if (e < -it.size())
      return null;
    if (b < -it.size())
      return null;
    PyListObject list = new PyListObject();
    int mod = it.size() + 1;
    if (s < 0) {
      for (int i = (b + mod) % mod; i > ((e + mod) % mod); i += s) {
        assert i >= 0;
        list.append(new PyLongObject(i));
      }
    } else {
      for (int i = ((b + mod) % mod); i < (e + mod) % mod; i += s) {
        assert i >= 0;
        list.append(new PyLongObject(i));
      }
    }
    return list;
  }
}
