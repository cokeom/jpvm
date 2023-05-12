package org.jpvm;

import org.jpvm.pycParser.CodeObject;
import org.jpvm.module.Marshal;
import org.jpvm.pycParser.PycReader;
import org.junit.Test;
import static org.junit.Assert.*;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

public class PycParserTest {

   @Test
   public void testPycReader() throws IOException {
      String filename = "src/test/resources/pys/__pycache__/add.cpython-38.pyc";
      PycReader reader = new PycReader(filename);
      reader.doParse();
      int magicNumber = reader.getMagicNumber();
      int bitFiled = reader.getBitFiled();
      int timestamp = reader.getTimestamp();
      int pyFileSize = reader.getMappingPyFileSize();
      Date date = new Date(timestamp * 1000L);
      System.out.println(date);
      // python3.8 magic number is 0xa0d0d55
      assertEquals(0xa0d0d55, magicNumber);
      assertEquals(0, bitFiled);
   }

   @Test
   public void marshalTest() throws IOException {
      String filename = "src/test/resources/pys/__pycache__/add.cpython-38.pyc";
      FileInputStream stream = new FileInputStream(filename);
      stream.skipNBytes(16);
      int available = stream.available();
      byte[] bytes = new byte[available];
      int s = stream.read(bytes);
      Marshal marshal = new Marshal();
      CodeObject codeObject = marshal.loadCodeObject(bytes);
      System.out.println(codeObject);
      assertEquals(0, codeObject.getCoArgument());
      assertEquals(0, codeObject.getCoKwOnlyArCnt());
      assertEquals(0, codeObject.getCoPosOnlyArCnt());
      assertEquals(0, codeObject.getCoNLocals());
      assertEquals(2, codeObject.getCoStackSize());
   }

   @Test
   public void syntaxTest() {
      byte c = (byte) 0xe3;
//      int d = c & 0xff;
      System.out.println(c);
      byte[] bytes = {1, 2, 3};
      byte[] bytes1 = new byte[1];
      ByteBuffer wrap = ByteBuffer.wrap(bytes);
      System.out.println(wrap.position());
      wrap.get(bytes1);
      System.out.println(wrap.position());
   }
}
