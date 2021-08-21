package org.ivance;

import static org.junit.Assert.*;

import org.ivance.python.PythonSandbox;
import org.ivance.python.PythonScriptUnsafeException;
import org.junit.Test;
import org.python.util.PythonInterpreter;

import java.lang.reflect.Field;

public class PythonSandboxTest {

    @Test
    public void initTest() {
       try {
           PythonSandbox sandbox = new PythonSandbox("test_obj = 'test string'");
           String result = sandbox.exec("print(test_obj)").trim();
           assertEquals(result, "test string");
       } catch (PythonScriptUnsafeException e) {
            fail();
       }
    }

    @Test
    public void keywordTest() {
        PythonSandbox sandbox = new PythonSandbox();
        try {
            sandbox.exec("import os");
            fail();
        } catch (PythonScriptUnsafeException ignored) {
        }
    }

    @Test
    public void bannedBuiltinsTest() throws NoSuchFieldException, IllegalAccessException {
        PythonSandbox sandbox = new PythonSandbox();
        Field interpreterField = PythonSandbox.class.getDeclaredField("interpreter");
        interpreterField.setAccessible(true);
        PythonInterpreter interpreter = (PythonInterpreter) interpreterField.get(sandbox);
        interpreter.exec("print(__builtins__.__dict__.get('open') == None)");
        assertEquals("True", sandbox.getBufferedOutput().trim());
    }

    @Test
    public void tryExecTest() {
        PythonSandbox sandbox = new PythonSandbox();
        String result = sandbox.tryExec("import os");
        assertEquals("Script contains keyword \"os\", which maybe unsafe", result);
    }

    @Test
    public void conditionTest() {
        PythonSandbox sandbox = new PythonSandbox();
        sandbox.tryExec("print(x)");
        assertEquals(PythonSandbox.ExecuteCondition.FAIL, sandbox.getLastExecuteCondition());
    }

    @Test
    public void resetTest() {
        PythonSandbox sandbox = new PythonSandbox();
        sandbox.tryExec("test_var = 'test'");
        sandbox.reset();
        assertNotEquals(sandbox.tryExec("print(test_var)").trim(), "test");
        try {
            sandbox.exec("import os");
            fail();
        } catch (PythonScriptUnsafeException ignore) {
        }
    }
}
