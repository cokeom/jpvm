package org.jpvm.testvm;

import org.jpvm.errors.PyException;
import org.jpvm.objects.PyFrameObject;
import org.jpvm.objects.PyObject;
import org.jpvm.pvm.EvaluationLoop;
import org.jpvm.pycParser.PyCodeObject;
import org.jpvm.pycParser.PycReader;
import org.jpvm.python.BuiltIn;
import org.junit.Test;

import java.io.IOException;

public class TestMethods {
    public PyObject test(String filename) throws IOException, PyException {
        PycReader reader = new PycReader(filename);
        reader.doParse();
        PyCodeObject codeObject = reader.getCodeObject();
        BuiltIn.doInit();
        PyFrameObject object = new PyFrameObject(codeObject, BuiltIn.dict, null);
        EvaluationLoop evaluationLoop = new EvaluationLoop(object);
        return evaluationLoop.pyEvalFrame();
    }

    @Test
    public void testHelloWorld() throws IOException {
        String filename = "src/test/resources/testpy/__pycache__/test01.cpython-38.pyc";
        PyObject ret = null;
        try {
            ret = test(filename);
        } catch (PyException e) {
            e.printStackTrace();
        }
        assert ret != null;
        System.out.println("ret = " + ret.str().getData());
    }
}
