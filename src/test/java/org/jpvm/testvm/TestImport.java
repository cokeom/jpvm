package org.jpvm.testvm;

import org.jpvm.errors.PyException;
import org.jpvm.pvm.PVM;
import org.junit.Test;

import java.io.IOException;

public class TestImport {

  @Test
  public void testImport() throws PyException, IOException {
    String filename = "src/test/resources/obsy/__pycache__/test04.cpython-38.pyc";
    new PVM(filename).run();
  }

  @Test
  public void testImport02() throws PyException, IOException {
    String filename = "src/test/resources/obsy/__pycache__/test05.cpython-38.pyc";
    new PVM(filename).run();
  }

  @Test
  public void testImport03() throws PyException, IOException {
    String filename = "src/test/resources/obsy/__pycache__/test07.cpython-38.pyc";
    new PVM(filename).run();
  }
}
